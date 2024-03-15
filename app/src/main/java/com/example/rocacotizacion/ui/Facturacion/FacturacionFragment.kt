package com.example.rocacotizacion.ui.Facturacion

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.rocacotizacion.R

class FacturacionFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_facturacion, container, false)

        val tipoPago = activity?.intent?.getStringExtra("tipoPago")
        val clienteNombre = activity?.intent?.getStringExtra("clienteNombre")
        Toast.makeText(context, "Pedido ${tipoPago} para : ${clienteNombre}", Toast.LENGTH_SHORT).show()

        // Use tipoPago and clienteNombre as needed

        return view
    }
}

