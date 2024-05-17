package com.example.rocacotizacion.ui.Facturacion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.rocacotizacion.R

class FacturacionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facturacion)

        // Retrieve the extras from the intent
        val tipoPago = intent.getStringExtra("tipoPago")
        val clienteNombre = intent.getStringExtra("clienteNombre")
        val clientecodigo = intent.getStringExtra("clientecodigo")

        // Create a new instance of FacturacionFragment with arguments
        val fragment = FacturacionFragment().apply {
            arguments = Bundle().apply {
                putString("tipoPago", tipoPago)
                putString("clienteNombre", clienteNombre)
                putString("clientecodigo", clientecodigo)
            }
        }

        // Replace the container with the FacturacionFragment with arguments
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
