package com.example.rocacotizacion.ui.Facturacion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.DAO.PedidoDtl
import com.example.rocacotizacion.DAO.PedidoHdr
import com.example.rocacotizacion.DTO.SharedDataModel
import com.example.rocacotizacion.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResumenFragment : Fragment() {
    private lateinit var resumenAdapter: ResumenAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_resumen, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewResumen)
        resumenAdapter = ResumenAdapter(listOf())
        recyclerView.adapter = resumenAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        var textViewtipopago:TextView=view.findViewById(R.id.tipopago)
        textViewtipopago.text= activity?.intent?.getStringExtra("tipoPago")
            ?.let { stringtipopago(it) }
        // Observe the detalleItems LiveData
        SharedDataModel.detalleItems.observe(viewLifecycleOwner, Observer { items ->
            resumenAdapter.updateItems(items)
            // Update subtotal and total
            val total = items.sumOf { it.subtotal }
            val roundedTotal = String.format("%.2f", total)
            view.findViewById<TextView>(R.id.sumsubtotal).text = "L.$roundedTotal"
            view.findViewById<TextView>(R.id.sumtotal).text = "L.$roundedTotal"
        })
        //accion del boton guardar btnsavepedido
         val btnsavepedido: Button = view.findViewById(R.id.btnsavepedido)
        btnsavepedido.setOnClickListener {
            if (SharedDataModel.detalleItems.value.isNullOrEmpty()) {
                Toast.makeText(context, "No hay items en el pedido", Toast.LENGTH_SHORT).show()
            } else {
                saveOrder()
            }
        }
        return view
    }
    fun stringtipopago(tipopago: String):String{
        if (tipopago=="CRED")
            return "Credito"
        else
            return "Contado"
    }
    private fun saveOrder() {
        CoroutineScope(Dispatchers.IO).launch {
            val tipoPago = activity?.intent?.getStringExtra("tipoPago") ?: "Contado"
            val detalleItems = SharedDataModel.detalleItems.value ?: listOf()

            val subtotal = detalleItems.sumOf { it.subtotal }
            val descuento = 0.0 // Calculate any discount if applicable
            val total = subtotal - descuento

            val pedidoHdr = PedidoHdr(tipopago = tipoPago, subtotal = subtotal, descuento = descuento, total = total)
            val hdrId = DatabaseApplication.getDatabase(requireContext()).PedidoHdrDAO().insertPedidoHdr(pedidoHdr)

            if (hdrId > 0) {
                // Insert was successful, now insert the details
                detalleItems.forEach { item ->
                    val pedidoDtl = PedidoDtl(
                        idhdr = hdrId.toInt(),
                        codigoproducto = item.codigoproducto,
                        cantidad = item.quantity,
                        precio = item.price,
                        descuento = 0.0 // Calculate any discount if applicable
                    )
                    DatabaseApplication.getDatabase(requireContext()).PedidoDtlDAO().insertPedidoDtl(pedidoDtl)
                    requireActivity().onBackPressed()

                }

                // Clear the SharedDataModel.detalleItems list
                SharedDataModel.detalleItems.postValue(mutableListOf())
            }
        }
    }



}
