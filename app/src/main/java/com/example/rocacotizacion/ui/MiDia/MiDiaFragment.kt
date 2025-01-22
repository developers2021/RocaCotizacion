package com.example.rocacotizacion.ui.MiDia

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.DataModel.PedidoHdrS
import com.example.rocacotizacion.DataModel.PedidoDtlS
import com.example.rocacotizacion.DataModel.SendPedido
import com.example.rocacotizacion.R
import com.example.rocacotizacion.Utilidades
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import com.example.rocacotizacion.Adapter.ConditionHandler
import com.example.rocacotizacion.ui.PrintClass.HtmlTemplates
import com.example.rocacotizacion.ui.home.HomeFragment
import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.tool.xml.XMLWorkerHelper
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MiDiaFragment : Fragment() {

    private lateinit var viewModel: PedidoViewModel
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout






    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_midia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // Inicializar ViewModel
            viewModel = ViewModelProvider(requireActivity()).get(PedidoViewModel::class.java)

            // Configurar ViewPager2 y TabLayout
            viewPager = view.findViewById(R.id.viewPager)
            tabLayout = view.findViewById(R.id.tabLayout)

            // Configurar botón dinámico
            val toolbarActionButton: ImageButton = view.findViewById(R.id.toolbar_action_button)

            // Asignar el Adapter al ViewPager2
            val adapter = MiDiaPagerAdapter(this)
            viewPager.adapter = adapter

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "Pedidos"
                    1 -> "Reporte de Cierre"
                    else -> ""
                }
            }.attach()

            // Additional UI setup such as Drawer and NavigationView
            setupDrawer(view)

            // Cambiar ícono dinámicamente según la pestaña activa
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    when (position) {
                        0 -> {
                            // Ícono de sincronización en "Pedidos"
                            toolbarActionButton.setImageResource(R.drawable.ic_cloud)
                            toolbarActionButton.setOnClickListener {
                                // Llamar a la función ya existente
                                CoroutineScope(Dispatchers.IO).launch {
                                    checkAndSyncPedidos()
                                }
                            }
                        }
                        1 -> {
                            // Ícono de impresión en "Cierre del Día"
                            toolbarActionButton.setImageResource(R.drawable.ic_print_icon)
                            toolbarActionButton.setOnClickListener {
                                // Lógica para impresión
                                printCierreDia()
                            }
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("MiDiaFragment", "Error en onViewCreated: ${e.message}", e)
        }
    }

    private fun printCierreDia() {
        CoroutineScope(Dispatchers.IO).launch {
            val agente = context?.let { DatabaseApplication.getDatabase(it).AgenteDAO().getAgente() }

            if (agente != null) {
                val pedidoHdrList = viewModel.pedidoHdrList.value ?: emptyList()
                val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(
                    Date()
                )
                val ruta = agente.rutadesc
                val vendedor = agente.descripcionLarga ?: "Sin nombre"

                var total = 0.0
                var contado = 0.0
                var credito = 0.0

                // Calcular los totales
                pedidoHdrList.forEach { pedido ->
                    total += pedido.total
                    when (pedido.tipopago) {
                        "CTADO" -> contado += pedido.total
                        "CRED" -> credito += pedido.total
                    }
                }

                // Generar HTML con `getHtmlForCierreDia`
                val htmlContent = HtmlTemplates.getHtmlForCierreDia(
                    fechaEmision = fechaActual,   // Corregir el nombre del parámetro
                    rutanombre = ruta,           // Cambiar a `rutanombre`
                    vendedornombre = vendedor,   // Cambiar a `vendedornombre`
                    total = total,
                    contado = contado,
                    credito = credito
                )


                // Generar PDF e imprimirlo
                val pdfStream = convertHtmlToPdf(htmlContent)
                val fileName = "CierreDelDia_${System.currentTimeMillis()}.pdf"
                savePdfToFile(requireContext(), pdfStream, fileName)
                val file = File(requireContext().getExternalFilesDir(null), fileName)
                openPdfWithExternalViewer(requireContext(), file)
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "No se encontraron datos del agente", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }






    private fun createJson(sendPedido: SendPedido): String {
        val root = JSONObject()
        root.put("idAgentes", sendPedido.idAgentes)
        root.put("codigoAgentes", sendPedido.codigoAgentes)
        root.put("idSucursal", sendPedido.idSucursal)
        root.put("idBodega", sendPedido.idBodega)
        root.put("usuarioCreacion", sendPedido.idAgentes)

        val pedidosArray = JSONArray()
        sendPedido.pedidos.forEach { pedidoHdr ->
            val pedidoHdrJson = JSONObject()
            pedidoHdrJson.put("id", pedidoHdr.id)
            pedidoHdrJson.put("TipoPago", pedidoHdr.tipoPago)
            pedidoHdrJson.put("idTipoVenta", pedidoHdr.tipoPago)
            pedidoHdrJson.put("subtotal", pedidoHdr.subtotal)
            pedidoHdrJson.put("descuento", pedidoHdr.descuento)
            pedidoHdrJson.put("total", pedidoHdr.total)
            pedidoHdrJson.put("clientecodigo", pedidoHdr.clientecodigo)
            pedidoHdrJson.put("isSincronizado", pedidoHdr.isSincronizado)
            pedidoHdrJson.put("impuesto", pedidoHdr.impuesto)
            pedidoHdrJson.put("codigopedido", pedidoHdr.codigopedido)
            pedidoHdrJson.put("anulado", pedidoHdr.anulado)
            pedidoHdrJson.put("usuario", pedidoHdr.ruta)
            pedidoHdrJson.put("rtncliente", pedidoHdr.rtncliente)
            pedidoHdrJson.put("nombrecliente", pedidoHdr.clientenombre)
            pedidoHdrJson.put("fecha", pedidoHdr.fecha)
            pedidoHdrJson.put("hora", pedidoHdr.hora)
            pedidoHdrJson.put("venCodigo", pedidoHdr.venCodigo)
            pedidoHdrJson.put("descuentorutaactivado", pedidoHdr.descuentorutaactivado)
            pedidoHdrJson.put("descuentoescalaactivado", pedidoHdr.descuentoescalaactivado)
            pedidoHdrJson.put("descuentotipopagoactivado", pedidoHdr.descuentotipopagoactivado)
            val pedidoDtlsArray = JSONArray()
            pedidoHdr.pedidoDtls.forEach { pedidoDtl ->
                val pedidoDtlJson = JSONObject()
                pedidoDtlJson.put("id", pedidoDtl.id)
                pedidoDtlJson.put("idhdr", pedidoDtl.idhdr)
                pedidoDtlJson.put("codigoProducto", pedidoDtl.codigoProducto)
                pedidoDtlJson.put("cantidad", pedidoDtl.cantidad)
                pedidoDtlJson.put("precio", pedidoDtl.precio)
                pedidoDtlJson.put("descuento", pedidoDtl.descuento)
                pedidoDtlJson.put("impuesto", pedidoDtl.impuesto)
                pedidoDtlsArray.put(pedidoDtlJson)
            }

            pedidoHdrJson.put("pedidoDtls", pedidoDtlsArray)
            pedidosArray.put(pedidoHdrJson)
        }

        root.put("pedidos", pedidosArray)

        return root.toString(4)
    }


    private suspend fun checkAndSyncPedidos() {
        val db = context?.let { DatabaseApplication.getDatabase(it) }
        db?.let { database ->
            val headers = database.PedidoHdrDAO().getAllPedidoHdrS()
            if (headers.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "No hay nada que sincronizar", Toast.LENGTH_SHORT).show()
                }
            } else {
                val agente = withContext(Dispatchers.IO) {
                    database.AgenteDAO().getAgente()
                }

                if (agente != null) {
                    val pedidos = headers.map { header ->
                        // Obtener detalles del pedido
                        val details = withContext(Dispatchers.IO) {
                            database.PedidoDtlDAO().getAllDetailsByHeaderId(header.id)
                        }

                        // Obtener datos del cliente
                        val cliente = withContext(Dispatchers.IO) {
                            database.ClientesDAO().getClientById(header.clientecodigo) // Ajustar si es necesario
                        }

                        // Obtener fecha y hora actuales en formatos compatibles con SQL Server
                        val currentDateTime = java.time.LocalDateTime.now()
                        val fechaFormatted = currentDateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        val horaFormatted = currentDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))

                        PedidoHdrS(
                            id = header.id,
                            tipoPago = header.tipopago,
                            subtotal = header.subtotal,
                            descuento = header.descuento,
                            total = header.total,
                            clientecodigo = header.clientecodigo,
                            isSincronizado = header.sinc,
                            impuesto = header.impuesto,
                            codigopedido = header.codigopedido,
                            anulado = header.anulado,
                            ruta = agente.username,
                            venCodigo = agente.codigoAuxiliar,
                            rtncliente = cliente?.Rtncliente,
                            clientenombre = cliente?.nombrecliente,
                            fecha = fechaFormatted, // Fecha en formato "yyyy-MM-dd"
                            hora = horaFormatted, // Hora en formato "HH:mm:ss"
                            descuentorutaactivado = header.descuentoRutaActivado,
                            descuentoescalaactivado = header.descuentoEscalaActivado,
                            descuentotipopagoactivado = header.descuentoTipoPagoActivado,
                            pedidoDtls = details.map { detail ->
                                PedidoDtlS(
                                    id = detail.id,
                                    idhdr = detail.idhdr,
                                    codigoProducto = detail.codigoproducto,
                                    cantidad = detail.cantidad,
                                    precio = detail.precio,
                                    descuento = detail.descuento,
                                    impuesto = detail.impuesto,
                                )
                            },

                        )
                    }

                    withContext(Dispatchers.Main) {
                        val sendPedido = SendPedido(
                            idAgentes = agente.idAgentes,
                            codigoAgentes = agente.codigoAgentes,
                            idSucursal = agente.idSucursal,
                            idBodega = agente.idbodega,
                            pedidos = pedidos
                        )
                        val jsonResult = createJson(sendPedido)
                        Log.d("Obj sendPedido", "sendPedido: $jsonResult")
                        sendPedido(jsonResult) {
                            Toast.makeText(
                                requireContext(),
                                "Sincronización de pedidos para agente: ${agente.username}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "No hay datos de agente disponibles.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } ?: withContext(Dispatchers.Main) {
            Toast.makeText(requireContext(), "Database access error", Toast.LENGTH_SHORT).show()
        }
    }








    private fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }

    private fun setupDrawer(view: View) {
        val drawerLayout: DrawerLayout = view.findViewById(R.id.drawer_layout_midia)
        val toolbar: Toolbar = view.findViewById(R.id.toolbar_midia)
        val toggle = ActionBarDrawerToggle(
            activity, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        val sharedPreferences = activity?.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val loggedInUsername = sharedPreferences?.getString("LoggedInUsername", null)
        val navigationView: NavigationView = view.findViewById(R.id.nav_midia)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    findNavController().navigate(R.id.nav_home)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_clientes -> {
                    findNavController().navigate(R.id.nav_clientes)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_gallery -> {
                    findNavController().navigate(R.id.nav_midia)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_slideshow -> {
                    //evaluando si puede cerrar sesion
                    ConditionHandler.showConfirmationDialog(requireContext())
                    true
                }
                else -> false
            }
        }
        loggedInUsername?.let { username ->
            HomeFragment.GetAgenteAsyncTask(requireContext(), username) { agente ->
                // This is your callback that gets executed on the main thread.
                // Update your UI here with the agent details.
                if (agente != null) {
                    navigationView.findViewById<TextView>(R.id.MenuName).text = "${agente.descripcionLarga}"
                    navigationView.findViewById<TextView>(R.id.textView).text = "${agente.descripcionCorta}"
                }
            }.execute()
        }
    }



    private fun sendPedido(jsonToSend: String, onComplete: () -> Unit) {
        val ids = extractIdsFromJson(jsonToSend)

        if (isOnline(requireContext())) {
            val okHttpClient = OkHttpClient()
            val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonToSend)

            val request = Request.Builder()
                .url(Utilidades.URL_SAVE_JSON_PEDIDO)
                .post(requestBody)
                .build()

            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    activity?.runOnUiThread {
                        showDialog(
                            title = "Error de Sincronización",
                            message = "No se pudo enviar los datos. Por favor, intente nuevamente.\nDetalle del error: ${e.message}"
                        )
                        onComplete()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        val responseBodyString = it.body?.string() ?: "Respuesta vacía"
                        activity?.runOnUiThread {
                            if (it.isSuccessful) {
                                try {
                                    // Parsear la respuesta como un objeto JSON
                                    val jsonResponse = JSONObject(responseBodyString)

                                    // Leer los campos directamente desde el JSON
                                    val success = jsonResponse.getBoolean("success")
                                    val message = jsonResponse.optString("message", "Mensaje no disponible.")

                                    if (success) {
                                        updateSincStatus(ids)
                                        showDialog(
                                            title = "Sincronización Exitosa",
                                            message = "Los pedidos se han sincronizado correctamente. Detalle: $message"
                                        )
                                    } else {
                                        val errorDetails = jsonResponse.optString("details", "Detalles no disponibles.")
                                        showDialog(
                                            title = "Error de Sincronización",
                                            message = "No se pudieron sincronizar los pedidos. Código: ${response.code}, Detalle: $message. Más info: $errorDetails"
                                        )
                                    }
                                } catch (e: Exception) {
                                    showDialog(
                                        title = "Error de Parseo",
                                        message = "No se pudo interpretar la respuesta del servidor. Código: ${response.code}, Respuesta: $responseBodyString"
                                    )
                                }
                            } else {
                                showDialog(
                                    title = "Error del Servidor",
                                    message = "Problema con el servidor. Código: ${response.code}, Respuesta: $responseBodyString"
                                )
                            }
                            onComplete()
                        }
                    }
                }





            })
        } else {
            activity?.runOnUiThread {
                showDialog(
                    title = "Sin Conexión",
                    message = "No hay conexión a internet."
                )
                onComplete()
            }
        }
    }


    private fun showDialog(title: String, message: String) {
        activity?.let {
            AlertDialog.Builder(it).apply {
                setTitle(title)
                setMessage(message)
                setPositiveButton("Aceptar") { dialog, _ ->
                    dialog.dismiss()
                }
                show()
            }
        }
    }

    private fun extractIdsFromJson(jsonString: String): List<Int> {
        val ids = mutableListOf<Int>()
        val jsonObject = JSONObject(jsonString)
        val pedidosArray = jsonObject.getJSONArray("pedidos")
        for (i in 0 until pedidosArray.length()) {
            val pedido = pedidosArray.getJSONObject(i)
            val id = pedido.getInt("id")
            ids.add(id)
        }
        return ids
    }

    private fun updateSincStatus(ids: List<Int>) {
        CoroutineScope(Dispatchers.IO).launch {
            context?.let {
                val db = DatabaseApplication.getDatabase(it)
                db.PedidoHdrDAO().updateSincForIds(ids, true)
            }
        }
    }

    fun navigateToDetallePedidoFromMiDia(pedidoId: Int) {
        val bundle = Bundle().apply {
            putInt("pedidoId", pedidoId)
        }
        try {
            findNavController().navigate(R.id.action_miDiaFragment_to_detallePedidoFragment, bundle)
        } catch (e: Exception) {
            Log.e("MiDiaFragment", "Error al navegar desde MiDiaFragment: ${e.message}")
            Toast.makeText(requireContext(), "Error al navegar al detalle del pedido", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para convertir HTML a PDF
    private fun convertHtmlToPdf(htmlContent: String): ByteArrayOutputStream {
        val outputStream = ByteArrayOutputStream()
        val document = Document()
        try {
            val pdfWriter = PdfWriter.getInstance(document, outputStream)
            document.open()
            XMLWorkerHelper.getInstance().parseXHtml(
                pdfWriter, document,
                ByteArrayInputStream(htmlContent.toByteArray(StandardCharsets.UTF_8))
            )
        } finally {
            document.close()
        }
        return outputStream
    }

    // Función para guardar el PDF en el sistema de archivos
    private fun savePdfToFile(context: Context, pdfStream: ByteArrayOutputStream, fileName: String) {
        val file = File(context.getExternalFilesDir(null), fileName)
        try {
            FileOutputStream(file).use { fileOutputStream ->
                pdfStream.writeTo(fileOutputStream)
            }
        } catch (e: Exception) {
            Log.e("PDF Creation", "Error al guardar el PDF", e)
        }
    }

    // Función para abrir el PDF con un visor externo
    private fun openPdfWithExternalViewer(context: Context, file: File) {
        val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }






}
