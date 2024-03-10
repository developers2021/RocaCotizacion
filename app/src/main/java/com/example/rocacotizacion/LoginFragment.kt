package com.example.rocacotizacion

import android.content.Context
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
import com.example.rocacotizacion.DAO.DatabaseApplication
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
        loginButton.setOnClickListener {
            val username = view.findViewById<EditText>(R.id.username).text.toString()
            val password = view.findViewById<EditText>(R.id.password).text.toString()
            if(username!="" && password!="")
            {
                sendLoginRequest(username, password)
            }
            else{
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
    fun handleUserLoginResponse(jsonString: String, context: Context) {
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
            nombodega= userJson.optString("nombodega")
            )

        // Now insert 'agente' into the database
        CoroutineScope(Dispatchers.IO).launch {
            // Ensure you get an instance of your Room database and then get the DAO instance
            val db = DatabaseApplication.getDatabase(context)
            db.AgenteDAO().insert(agente) // Use the DAO's insert method directly
        }
    }
    private fun sendLoginRequest(username: String, password: String) {

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
                                            handleUserLoginResponse(responseBodyString, it1   )
                                        }
                                        Toast.makeText(context, "Bienvenido $username", Toast.LENGTH_LONG).show()
                                        findNavController().navigate(R.id.nav_home)
                                    }
                                } else {
                                    // Login failed, show the message to the user
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            // Handle the case where the server responded with an error status
                            activity?.runOnUiThread {
                                // Update UI to show an error message
                            }
                        }
                    }
                }
            })

        } else {
            // Show error message
            Toast.makeText(requireContext(), "Revise su Conexión a Internet", Toast.LENGTH_SHORT).show()
        }


    }
}
