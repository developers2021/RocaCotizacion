package com.example.rocacotizacion.ui.ListaProducto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rocacotizacion.R
import com.example.rocacotizacion.ui.ListaProducto.ListaProductoFragment

class ListaProductoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listaproductos)

        // Get the data from intent or other sources
        val tipoPago = intent.getStringExtra("tipoPago")
        val clienteNombre = intent.getStringExtra("clienteNombre")

        // Create a new instance of ListaProductoFragment with arguments
        val fragment = ListaProductoFragment().apply {
            arguments = Bundle().apply {
                putString("tipoPago", tipoPago)
                putString("clienteNombre", clienteNombre)
            }
        }

        // Add the fragment to the activity
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_listaproductos, fragment)
            .commit()
    }
}
