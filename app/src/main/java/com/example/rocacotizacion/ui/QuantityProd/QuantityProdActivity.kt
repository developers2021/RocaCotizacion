package com.example.rocacotizacion.ui.QuantityProd

import QuantityProdFragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rocacotizacion.R
import com.example.rocacotizacion.ui.ListaProducto.ListaProductoFragment

class QuantityProdActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quantity_prod)
        val tipoPago = intent.getStringExtra("tipoPago")
        val idproducto = intent.getStringExtra("idproducto")

        // Create a new instance of ListaProductoFragment with arguments
        val fragment = QuantityProdFragment().apply {
            arguments = Bundle().apply {
                putString("tipoPago", tipoPago)
                putString("idproducto", idproducto)
            }
        }
        // Add the fragment to the activity
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_quantity_prod, fragment)
            .commit()
    }
}