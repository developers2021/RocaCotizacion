package com.example.rocacotizacion.ui.Facturacion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.rocacotizacion.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DetalleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalles, container, false)

        val fab: FloatingActionButton = view.findViewById(R.id.fab_add)
        fab.setOnClickListener {
            // Handle the add action
            Toast.makeText(context, "Add Product", Toast.LENGTH_SHORT).show()
            // Implement your logic to add a product here
        }

        return view
    }
}
