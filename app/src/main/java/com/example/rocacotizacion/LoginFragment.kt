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
                            val result = json.getBoolean("result")
                            val message = json.getString("message")

                            activity?.runOnUiThread {
                                // Update your UI based on 'result' and 'message'
                                if (result) {
                                    // Login success
                                    Toast.makeText(context, "Bienvenido $username", Toast.LENGTH_LONG).show()
                                    findNavController().navigate(R.id.nav_home)
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
