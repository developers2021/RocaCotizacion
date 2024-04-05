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
        val textViewProducto: TextView = view.findViewById(R.id.textViewProducto)
        val textViewPrecio: TextView = view.findViewById(R.id.textViewPrecio)
        val textViewsubtotal: TextView = view.findViewById(R.id.textViewsubtotal)
        val sumsubtotal: TextView = view.findViewById(R.id.sumsubtotal)
        val sumtotal:TextView=view.findViewById(R.id.sumtotal)

        var textViewtipopago:TextView=view.findViewById(R.id.tipopago)

        textViewtipopago.text= activity?.intent?.getStringExtra("tipoPago")
            ?.let { stringtipopago(it) }

        // Observe the detalleItems LiveData
        SharedDataModel.detalleItems.observe(viewLifecycleOwner, Observer { items ->
            textViewProducto.text = SharedDataModel.getproducto()
            textViewPrecio.text = SharedDataModel.getprice()
            textViewsubtotal.text = SharedDataModel.getsubtotal()
            sumsubtotal.text=SharedDataModel.getTotalPrice()
            sumtotal.text=SharedDataModel.getTotalPrice()
        })
        return view
    }
    fun stringtipopago(tipopago: String):String{
        if (tipopago=="CRED")
            return "Credito"
        else
            return "Contado"
    }
}
