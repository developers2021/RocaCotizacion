package com.example.rocacotizacion.ui.Facturacion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.rocacotizacion.R

class FacturacionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facturacion)

        // Recupera los extras del Intent
        val modo = intent.getStringExtra("modo")
        val tipoPago = intent.getStringExtra("tipoPago")
        val clienteNombre = intent.getStringExtra("clienteNombre")
        val clientecodigo = intent.getStringExtra("clientecodigo")

        // Crear la instancia del fragmento según el modo
        val fragment = if (modo == "editar") {
            // Recupera el pedidoId para edición
            val pedidoId = intent.getIntExtra("pedidoId", -1)
            // Usa el método de fábrica para modo edición (ver siguiente paso)
            FacturacionFragment.newInstanceEditMode(pedidoId, tipoPago, clienteNombre, clientecodigo)
        } else {
            // Modo creación
            FacturacionFragment.newInstanceCreateMode(tipoPago, clienteNombre, clientecodigo)
        }

        // Reemplazar el contenedor con el fragmento creado
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}

