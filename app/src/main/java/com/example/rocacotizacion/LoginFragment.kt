package com.example.rocacotizacion

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.rocacotizacion.DAO.Agente
import com.example.rocacotizacion.DAO.Clientes
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.DAO.Grupos
import com.example.rocacotizacion.DAO.NivelPrecioPredeterminado
import com.example.rocacotizacion.DAO.PreciosNivelTipoVenta
import com.example.rocacotizacion.DAO.Productos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException




class LoginFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginButton: Button = view.findViewById(R.id.login_button)
        val progressDialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_progress)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        loginButton.setOnClickListener {
            val username = view.findViewById<EditText>(R.id.username).text.toString()
            val password = view.findViewById<EditText>(R.id.password).text.toString()
            if (username != "" && password != "") {
                progressDialog.show()

                sendLoginRequest(username, password) {
                    requireActivity().runOnUiThread {
                        progressDialog.dismiss()
                    }
                }
            } else {
                Toast.makeText(context, "Datos Invalidos", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }
    // Assuming 'context' is the application context or an activity context that you pass to the function
    fun InsertDataDB(jsonString: String, context: Context) {
        val jsonObject = JSONObject(jsonString)

        val userJson = jsonObject.getJSONObject("user")
        val agente = Agente(
            idAgentes = userJson.getInt("idagentes"),
            codigoAgentes = userJson.optString("codigoagentes"),
            idSucursal = userJson.getInt("idsucursal"),
            idTipoAgente = userJson.getInt("idtipoagente"),
            cuentaContable = userJson.optString("cuentacontable"),
            flagPos = userJson.optBoolean("flagpos"),
            pctgeComisVenta = userJson.optDouble("pctgecomisventa"),
            descripcionCorta = userJson.optString("descripcioncorta"),
            descripcionLarga = userJson.optString("descripcionlarga"),
            // ... handle the rest of the fields similarly ...
            flagActivo = userJson.getBoolean("flagactivo"),
            ordenReporte = userJson.getInt("ordenreporte"),
            username = userJson.optString("username"),
            codigoAuxiliar = userJson.optString("codigoauxiliar"),
            usuarioCreacion = userJson.optString("usuariocreacion"),
            usuarioModificacion = userJson.optString("usuariomodificacion"),
            password = userJson.optString("password"),
            rutadesc = userJson.optString("rutadesc"),
            nombodega= userJson.optString("nombodega"),
            tipoagente = userJson.optString("tipoagente"),
            idbodega = userJson.optString("idbodega")
            )
        val clienteJsonArray = jsonObject.getJSONArray("clnClientes")
        val clientesList = mutableListOf<Clientes>()

        for (i in 0 until clienteJsonArray.length()) {
            val clienteJsonObject = clienteJsonArray.getJSONObject(i)
            val cliente = Clientes(
                idcliente = clienteJsonObject.getInt("idcliente"),
                nombrecliente = clienteJsonObject.optString("nombrecliente"),
                Codigocliente = clienteJsonObject.optString("codigocliente"),
                Rtncliente = clienteJsonObject.optString("rtncliente")
            )
            clientesList.add(cliente)
        }
        val ProductoJsonArray = jsonObject.getJSONArray("productos")
        val ProductosList = mutableListOf<Productos>()

        for (i in 0 until ProductoJsonArray.length()) {
            val clienteJsonObject = ProductoJsonArray.getJSONObject(i)
            val producto = Productos(
                idproducto = clienteJsonObject.getInt("idproducto"),
                codigoproducto = clienteJsonObject.optString("codigoproducto"),
                producto = clienteJsonObject.optString("producto"),
                idgrupo = clienteJsonObject.getInt("idgrupo"),
                grupo = clienteJsonObject.optString("grupo"),
                idtipoproducto = clienteJsonObject.getInt("idtipoproducto"),
                costoactual = clienteJsonObject.optDouble("costoactual"),
                idimpuesto = clienteJsonObject.getInt("idimpuesto"),
                porcentajeimpuesto = clienteJsonObject.optDouble("porcentajeimpuesto"),
                precio = clienteJsonObject.optDouble("precio"),
                descuento = clienteJsonObject.optDouble("descuento")
            )

            ProductosList.add(producto)
        }

        val GruposJsonArray = jsonObject.getJSONArray("grupos")
        val GruposList = mutableListOf<Grupos>()

        for (i in 0 until GruposJsonArray.length()) {
            val clienteJsonObject = GruposJsonArray.getJSONObject(i)
            val producto = Grupos(
                idgrupo = clienteJsonObject.getInt("idgrupo"),
                grupo = clienteJsonObject.optString("grupo")
            )
            GruposList.add(producto)
        }
        val PreciosNivelTipoVentaJsonArray = jsonObject.getJSONArray("preciosNivelTipoVenta")
        val PreciosNivelTipoVentaList = mutableListOf<PreciosNivelTipoVenta>()

        for (i in 0 until PreciosNivelTipoVentaJsonArray.length()) {
            val clienteJsonObject = PreciosNivelTipoVentaJsonArray.getJSONObject(i)
            val producto = PreciosNivelTipoVenta(
                idproducto = clienteJsonObject.getInt("idproducto"),
                precio = clienteJsonObject.optDouble("precio"),
                idnivelprecio = clienteJsonObject.getInt("idnivelprecio"),
                idtipoventa = clienteJsonObject.getInt("idtipoventa"),
                nivelprecio = clienteJsonObject.optString("nivelprecio"),
                tipoventa = clienteJsonObject.optString("tipoventa"),
                codigotipoventa = clienteJsonObject.optString("codigotipoventa"),
            )
            PreciosNivelTipoVentaList.add(producto)
        }

        val NivelPrecioPredeterminadoJsonArray = jsonObject.getJSONArray("nivelPrecioPredeterminado")
        val NivelPrecioPredeterminadoList = mutableListOf<NivelPrecioPredeterminado>()

        for (i in 0 until NivelPrecioPredeterminadoJsonArray.length()) {
            val clienteJsonObject = NivelPrecioPredeterminadoJsonArray.getJSONObject(i)
            val producto = NivelPrecioPredeterminado(
                id = clienteJsonObject.getInt("id"),
                text = clienteJsonObject.optString("text")
            )
            NivelPrecioPredeterminadoList.add(producto)
        }

        // Now insert the data into the database
        CoroutineScope(Dispatchers.IO).launch {
            // Ensure you get an instance of your Room database and then get the DAO instance
            // Use the DAO's insert method directly
            val db = DatabaseApplication.getDatabase(context)
            db.AgenteDAO().insert(agente)
            db.ClientesDAO().insertAll(clientesList)
            db.ProductosDAO().insertAll(ProductosList)
            db.GrupoDAO().insertAll(GruposList)
            db.PreciosNivelTipoVentaDAO().insertAll(PreciosNivelTipoVentaList)
            db.NivelPrecioPredeterminadoDAO().insertAll(NivelPrecioPredeterminadoList)
        }
    }
    private fun sendLoginRequest(username: String, password: String, onComplete: () -> Unit) {

        if (isOnline(requireContext())) {
            // Proceed with login process (e.g., make API call)
            val okHttpClient = OkHttpClient()
            val json = """{"username":"$username","password":"$password"}"""
            val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json)

            val request = Request.Builder()
                .url(Utilidades.URL_AUTH_LOGIN)
                .post(requestBody)
                .build()

            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle the error
                    e.printStackTrace()
                    activity?.runOnUiThread {
                        onComplete() // Call the callback function
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (it.isSuccessful) {
                            val responseBodyString = it.body?.string()

                            // Parsing the JSON response
                            val json = JSONObject(responseBodyString)
                            val resultObject = json.getJSONObject("result")
                            val result = resultObject.getBoolean("result")
                            val message = resultObject.getString("message")

                            activity?.runOnUiThread {
                                // Update your UI based on 'result' and 'message'
                                if (result) {
                                    // Login success
                                    val sharedPreferences = activity?.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
                                    sharedPreferences?.edit()?.putString("LoggedInUsername", username)?.apply()
                                    if (responseBodyString != null) {
                                        context?.let { it1 ->
                                            InsertDataDB(responseBodyString, it1)
                                        }
                                        Toast.makeText(context, "Bienvenido $username", Toast.LENGTH_LONG).show()
                                        findNavController().navigate(R.id.nav_home)
                                    }
                                } else {
                                    // Login failed, show the message to the user
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                                onComplete() // Call the callback function
                            }
                        } else {
                            // Handle the case where the server responded with an error status
                            activity?.runOnUiThread {
                                // Update UI to show an error message
                                onComplete() // Call the callback function
                            }
                        }
                    }
                }
            })

        } else {
            // Show error message
            Toast.makeText(requireContext(), "Revise su Conexión a Internet", Toast.LENGTH_SHORT).show()
            onComplete() // Call the callback function even if there's no internet connection
        }
    }

}
