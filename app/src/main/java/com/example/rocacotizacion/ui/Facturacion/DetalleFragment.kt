package com.example.rocacotizacion.ui.Facturacion

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.rocacotizacion.R
import com.example.rocacotizacion.ui.ListaProducto.ListaProductoActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DetalleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalles, container, false)

        val fab: FloatingActionButton = view.findViewById(R.id.fab_add)
        fab.setOnClickListener {
            val intent = Intent(context, ListaProductoActivity::class.java)

            // Retrieve the tipoPago from arguments
            val tipoPago = arguments?.getString("tipoPago")

            // Pass tipoPago to ListaProductoActivity
            intent.putExtra("tipoPago", tipoPago)
            startActivity(intent)
        }

        return view
    }
}

