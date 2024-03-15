package com.example.rocacotizacion.ui.Facturacion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.rocacotizacion.R

class FacturacionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facturacion)
        // Replace the container with the FacturacionFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, FacturacionFragment())
            .commit()
    }
}
