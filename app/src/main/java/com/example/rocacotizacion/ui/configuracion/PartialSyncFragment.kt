package com.example.rocacotizacion

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.rocacotizacion.DAO.Clientes
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.DAO.Grupos
import com.example.rocacotizacion.DAO.NivelPrecioPredeterminado
import com.example.rocacotizacion.DAO.PreciosNivelTipoVenta
import com.example.rocacotizacion.DAO.Productos
import com.example.rocacotizacion.DAO.invdescuentoporescala
import com.example.rocacotizacion.DAO.invdescuentoporruta
import com.example.rocacotizacion.DAO.invdescuentoportipoventa
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class PartialSyncFragment : Fragment() {

    private lateinit var llSyncClientes: LinearLayout
    private lateinit var llSyncProductos: LinearLayout
    private lateinit var llSyncPreciosGrupos: LinearLayout
    private lateinit var llSyncDescuentos: LinearLayout
    private lateinit var progressDialog: Dialog

    // Variables para valores de la tabla Agente
    private var idAgente: Int = -1
    private var idBodega: Int = -1
    private var idSucursal: Int = -1
    private var idRuta: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_partial_sync, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lógica IO para leer datos de Agente
        CoroutineScope(Dispatchers.IO).launch {
            val db = DatabaseApplication.getDatabase(requireContext())
            val agente = db.AgenteDAO().getAgente() // Devuelve Agente o null

            if (agente == null) {
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "No hay agente en la base de datos", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            // Asigna valores dinámicos
            idAgente = agente.idAgentes
            idBodega = agente.idbodega.toIntOrNull() ?: -1
            idSucursal = agente.idSucursal
            idRuta = agente.idruta

            // Configuración de UI en el hilo principal
            requireActivity().runOnUiThread {
                // Inicializa vistas
                llSyncClientes = view.findViewById(R.id.ll_sync_clientes)
                llSyncProductos = view.findViewById(R.id.ll_sync_productos)
                llSyncPreciosGrupos = view.findViewById(R.id.ll_sync_precios_grupos)
                llSyncDescuentos = view.findViewById(R.id.ll_sync_descuentos)

                // Configura el progress dialog
                progressDialog = Dialog(requireContext()).apply {
                    setContentView(R.layout.dialog_sync)
                    setCancelable(false)
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }

                // Listeners de sincronización
                llSyncClientes.setOnClickListener {
                    progressDialog.show()
                    syncClientes {
                        requireActivity().runOnUiThread {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Clientes sincronizados", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                llSyncProductos.setOnClickListener {
                    progressDialog.show()
                    syncProductos {
                        requireActivity().runOnUiThread {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Productos sincronizados", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                llSyncPreciosGrupos.setOnClickListener {
                    progressDialog.show()
                    syncPreciosGrupos {
                        requireActivity().runOnUiThread {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Precios y grupos sincronizados", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                llSyncDescuentos.setOnClickListener {
                    progressDialog.show()
                    syncDescuentos {
                        requireActivity().runOnUiThread {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Descuentos sincronizados", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    // ---------------------------
    // Sincronización de Clientes
    // ---------------------------
    private fun syncClientes(onComplete: () -> Unit) {
        if (idAgente <= 0) {
            Toast.makeText(requireContext(), "idAgente inválido", Toast.LENGTH_SHORT).show()
            onComplete()
            return
        }

        val url = "${Utilidades.URL_CLIENTES}?idAgente=$idAgente"
        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                onComplete()
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val responseBodyString = it.body?.string()
                        if (!responseBodyString.isNullOrEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val db = DatabaseApplication.getDatabase(requireContext())
                                    db.ClientesDAO().deleteAll()

                                    // En el JSON viene { "clientes": [...] }
                                    val clientesList = parseClientes(responseBodyString)
                                    db.ClientesDAO().insertAll(clientesList)
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                } finally {
                                    requireActivity().runOnUiThread { onComplete() }
                                }
                            }
                        } else {
                            onComplete()
                        }
                    } else {
                        onComplete()
                    }
                }
            }
        })
    }

    private fun parseClientes(response: String): List<Clientes> {
        val jsonObject = JSONObject(response)
        val array = jsonObject.getJSONArray("clientes")
        val list = mutableListOf<Clientes>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val cliente = Clientes(
                idcliente = item.getInt("idcliente"),
                nombrecliente = item.optString("nombrecliente"),
                Codigocliente = item.optString("codigocliente"),
                Rtncliente = item.optString("rtncliente"),
                totalValorSaldoFactura = item.optDouble("totalValorSaldoFactura")
            )
            list.add(cliente)
        }
        return list
    }

    // ---------------------------
// Sincronización de Productos
// ---------------------------
    private fun syncProductos(onComplete: () -> Unit) {
        if (idBodega <= 0) {
            Toast.makeText(requireContext(), "idBodega inválido", Toast.LENGTH_SHORT).show()
            onComplete()
            return
        }

        val url = "${Utilidades.URL_PRODUCTOS}?idBodega=$idBodega"
        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                onComplete()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val responseBodyString = it.body?.string()
                        if (!responseBodyString.isNullOrEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val db = DatabaseApplication.getDatabase(requireContext())

                                    // Borramos todo para volver a insertar.
                                    db.ProductosDAO().deleteAll()
                                    db.GrupoDAO().deleteAll()

                                    // Parsear { "productos": [...], "grupos": [...] }
                                    val (productosList, gruposList) = parseProductosYGrupos(responseBodyString)

                                    // Insertar en tablas
                                    db.ProductosDAO().insertAll(productosList)
                                    db.GrupoDAO().insertAll(gruposList)

                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                } finally {
                                    requireActivity().runOnUiThread { onComplete() }
                                }
                            }
                        } else {
                            // Respuesta vacía
                            onComplete()
                        }
                    } else {
                        // Error de servidor
                        onComplete()
                    }
                }
            }
        })
    }

    /**
     * Parsea el JSON { "productos": [...], "grupos": [...] } y regresa un Pair<Productos, Grupos>.
     */
    private fun parseProductosYGrupos(response: String): Pair<List<Productos>, List<Grupos>> {
        val jsonObject = JSONObject(response)

        // Parsear lista de productos
        val productosArray = jsonObject.getJSONArray("productos")
        val productosList = mutableListOf<Productos>()
        for (i in 0 until productosArray.length()) {
            val item = productosArray.getJSONObject(i)
            val producto = Productos(
                idproducto = item.getInt("idproducto"),
                codigoproducto = item.optString("codigoproducto"),
                producto = item.optString("producto"),
                idgrupo = item.getInt("idgrupo"),
                grupo = item.optString("grupo"),
                idtipoproducto = item.getInt("idtipoproducto"),
                costoactual = item.optDouble("costoactual"),
                idimpuesto = item.getInt("idimpuesto"),
                porcentajeimpuesto = item.optDouble("porcentajeimpuesto"),
                precio = item.optDouble("precio"),
                descuento = item.optDouble("descuento")
            )
            productosList.add(producto)
        }

        // Parsear lista de grupos
        val gruposArray = jsonObject.getJSONArray("grupos")
        val gruposList = mutableListOf<Grupos>()
        for (i in 0 until gruposArray.length()) {
            val item = gruposArray.getJSONObject(i)
            val grupo = Grupos(
                idgrupo = item.getInt("idgrupo"),
                grupo = item.optString("grupo")
            )
            gruposList.add(grupo)
        }

        // Retornamos ambas listas
        return Pair(productosList, gruposList)
    }

    // ---------------------------
// Sincronización de Precios
// ---------------------------
    private fun syncPreciosGrupos(onComplete: () -> Unit) {
        if (idSucursal <= 0 || idBodega <= 0) {
            Toast.makeText(requireContext(), "idSucursal o idBodega inválidos", Toast.LENGTH_SHORT).show()
            onComplete()
            return
        }

        val url = "${Utilidades.URL_PRECIOS_GRUPOS}?idSucursal=$idSucursal&idBodega=$idBodega"
        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                onComplete()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val responseBodyString = it.body?.string()
                        if (!responseBodyString.isNullOrEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val db = DatabaseApplication.getDatabase(requireContext())

                                    // Borrar datos previos de precios
                                    db.PreciosNivelTipoVentaDAO().deleteAll()
                                    db.NivelPrecioPredeterminadoDAO().deleteAll()

                                    // { "preciosNivelTipoVenta": [...], "nivelPrecioPredeterminado": [...] }
                                    val preciosList = parsePreciosNivelTipoVenta(responseBodyString)
                                    val nivelList = parseNivelPrecioPredeterminado(responseBodyString)

                                    db.PreciosNivelTipoVentaDAO().insertAll(preciosList)
                                    db.NivelPrecioPredeterminadoDAO().insertAll(nivelList)
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                } finally {
                                    requireActivity().runOnUiThread { onComplete() }
                                }
                            }
                        } else {
                            onComplete()
                        }
                    } else {
                        onComplete()
                    }
                }
            }
        })
    }

    private fun parsePreciosNivelTipoVenta(response: String): List<PreciosNivelTipoVenta> {
        val jsonObject = JSONObject(response)
        val array = jsonObject.getJSONArray("preciosNivelTipoVenta")
        val list = mutableListOf<PreciosNivelTipoVenta>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val precioNivel = PreciosNivelTipoVenta(
                idproducto = item.getInt("idproducto"),
                precio = item.optDouble("precio"),
                idnivelprecio = item.getInt("idnivelprecio"),
                idtipoventa = item.getInt("idtipoventa"),
                nivelprecio = item.optString("nivelprecio"),
                tipoventa = item.optString("tipoventa"),
                codigotipoventa = item.optString("codigotipoventa")
            )
            list.add(precioNivel)
        }
        return list
    }

    private fun parseNivelPrecioPredeterminado(response: String): List<NivelPrecioPredeterminado> {
        val jsonObject = JSONObject(response)
        val array = jsonObject.getJSONArray("nivelPrecioPredeterminado")
        val list = mutableListOf<NivelPrecioPredeterminado>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val nivel = NivelPrecioPredeterminado(
                id = item.getInt("id"),
                text = item.optString("text")
            )
            list.add(nivel)
        }
        return list
    }


    // ---------------------------
    // Sincronización de Descuentos
    // ---------------------------
    private fun syncDescuentos(onComplete: () -> Unit) {
        if (idBodega <= 0 || idRuta <= 0) {
            Toast.makeText(requireContext(), "idBodega o idRuta inválidos", Toast.LENGTH_SHORT).show()
            onComplete()
            return
        }

        val url = "${Utilidades.URL_DESCUENTOS}?idBodega=$idBodega&idRuta=$idRuta"
        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                onComplete()
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val responseBodyString = it.body?.string()
                        if (!responseBodyString.isNullOrEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val db = DatabaseApplication.getDatabase(requireContext())

                                    db.invdescuentoportipoventaDAO().deleteAll()
                                    db.invdescuentoporescalaDAO().deleteAll()
                                    db.invdescuentoporrutaDAO().deleteAll()

                                    // El JSON: { "descuentosPorTipoVenta": [...], "descuentosPorEscala": [...], "descuentosPorRuta": [...] }
                                    val tipoVentaList = parseDescuentosTipoVenta(responseBodyString)
                                    val escalaList = parseDescuentosEscala(responseBodyString)
                                    val rutaList = parseDescuentosRuta(responseBodyString)

                                    db.invdescuentoportipoventaDAO().insertAll(tipoVentaList)
                                    db.invdescuentoporescalaDAO().insertAll(escalaList)
                                    db.invdescuentoporrutaDAO().insertAll(rutaList)
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                } finally {
                                    requireActivity().runOnUiThread { onComplete() }
                                }
                            }
                        } else {
                            onComplete()
                        }
                    } else {
                        onComplete()
                    }
                }
            }
        })
    }

    private fun parseDescuentosTipoVenta(response: String): List<invdescuentoportipoventa> {
        val jsonObject = JSONObject(response)
        val array = jsonObject.getJSONArray("descuentosPorTipoVenta")
        val list = mutableListOf<invdescuentoportipoventa>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val descuento = invdescuentoportipoventa(
                idtipoventa = item.getInt("idtipoventa"),
                idpromocion = item.getInt("idpromocion"),
                promocion = item.optString("promocion"),
                codigotipopromocion = item.optString("codigotipopromocion"),
                tipopromocion = item.optString("tipopromocion"),
                idproducto = item.getInt("idproducto"),
                monto = item.getDouble("monto"),
                codigoproducto = item.optString("codigoproducto"),
                codigotipoventa = item.optString("codigotipoventa"),
                fechaInicio = item.optString("fechainicio", "-"),
                fechaFin = item.optString("fechafin", "-")
            )
            list.add(descuento)
        }
        return list
    }

    private fun parseDescuentosEscala(response: String): List<invdescuentoporescala> {
        val jsonObject = JSONObject(response)
        val array = jsonObject.getJSONArray("descuentosPorEscala")
        val list = mutableListOf<invdescuentoporescala>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val descuento = invdescuentoporescala(
                idpromocion = item.getInt("idpromocion"),
                promocion = item.optString("promocion"),
                codigotipopromocion = item.optString("codigotipopromocion"),
                tipopromocion = item.optString("tipopromocion"),
                idproducto = item.getInt("idproducto"),
                monto = item.getDouble("monto"),
                rangoinicial = item.getInt("rangoinicial"),
                rangofinal = item.getInt("rangofinal"),
                codigoproducto = item.optString("codigoproducto"),
                fechaInicio = item.optString("fechaInicio"),
                fechaFin = item.optString("fechaFin")
            )
            list.add(descuento)
        }
        return list
    }

    private fun parseDescuentosRuta(response: String): List<invdescuentoporruta> {
        val jsonObject = JSONObject(response)
        val array = jsonObject.getJSONArray("descuentosPorRuta")
        val list = mutableListOf<invdescuentoporruta>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val descuento = invdescuentoporruta(
                idpromocion = item.getInt("idpromocion"),
                promocion = item.optString("promocion"),
                codigotipopromocion = item.optString("codigotipopromocion"),
                tipopromocion = item.optString("tipopromocion"),
                idproducto = item.getInt("idproducto"),
                monto = item.getDouble("monto"),
                codigoproducto = item.optString("codigoproducto"),
                idruta = item.getInt("idRuta"),
                fechaInicio = item.optString("fechainicio", "-"),
                fechaFin = item.optString("fechafin", "-")
            )
            list.add(descuento)
        }
        return list
    }
}
