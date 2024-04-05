package com.example.rocacotizacion.ui.Facturacion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.rocacotizacion.DTO.SharedDataModel
import com.example.rocacotizacion.R

class ResumenFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_resumen, container, false)
        val textViewResumen: TextView = view.findViewById(R.id.textViewResumen)

        // Observe the detalleItems LiveData
        SharedDataModel.detalleItems.observe(viewLifecycleOwner, Observer { items ->
            val resumenText = SharedDataModel.getResumenText()
            textViewResumen.text = resumenText
        })

        return view
    }
}
