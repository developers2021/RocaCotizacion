
package com.example.rocacotizacion.ui.Facturacion

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
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
import kotlinx.coroutines.withContext

class ResumenFragment : Fragment() {
    private lateinit var resumenAdapter: ResumenAdapter
    private var isEscalaDiscountEnabled: Boolean = false

    private fun applyEscalaDiscounts() {
        CoroutineScope(Dispatchers.IO).launch {
            SharedDataModel.detalleItems.value?.forEach { item ->
                val escalaDiscounts = DatabaseApplication.getDatabase(requireContext())
                    .invdescuentoporescalaDAO()
                    .getDescuentoPorEscala(item.codigoproducto)
                val escalaDiscount = escalaDiscounts.firstOrNull {
                    item.quantity >= it.rangoinicial && item.quantity <= it.rangofinal
                }
               item.porcentajeEscala= escalaDiscount?.monto ?: (0.0 / 100)
               item.porcentajeTotal=item.porcentajeEscala+item.porcentajeTipoPago
               item.descuento=item.subtotal*(item.porcentajeTotal/100)
               item.subtotal=(item.price*item.quantity)-item.descuento
               item.checkedDescuentoEscala=true
            }
            SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)
            withContext(Dispatchers.Main) {
                updateTotals()
            }
        }
    }
    private fun applyTipoVentaDiscounts() {
        CoroutineScope(Dispatchers.IO).launch {
            SharedDataModel.detalleItems.value?.forEach { item ->
                val discountData = DatabaseApplication.getDatabase(requireContext())
                    .invdescuentoportipoventaDAO()
                    .getDescuentoPorTipoVenta(item.codigoproducto)
                item.porcentajeTipoPago= discountData?.monto ?: (0.0 / 100)
                item.porcentajeTotal=item.porcentajeEscala+item.porcentajeTipoPago
                item.descuento=(item.price*item.quantity)*(item.porcentajeTotal/100)
                item.subtotal=(item.price*item.quantity)-item.descuento
                item.checkedDescuentoTipoPago=true
            }
            SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)
            withContext(Dispatchers.Main) {
                updateTotals()
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val switchEscala = view.findViewById<SwitchCompat>(R.id.switchOption1)
        val switchTipoPago = view.findViewById<SwitchCompat>(R.id.switchOption2)

        switchEscala.setOnCheckedChangeListener { _, isChecked ->
            isEscalaDiscountEnabled = isChecked
            if (isChecked) {
                applyEscalaDiscounts()
            } else {
                removeEscalaDiscounts()
                updateTotals()
            }
        }

        switchTipoPago.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                applyTipoVentaDiscounts()
            } else {
                SharedDataModel.detalleItems.value?.forEach {
                    it.porcentajeTotal=it.porcentajeEscala
                    it.porcentajeTipoPago = 0.0
                    it.descuento=it.price*it.quantity*(it.porcentajeEscala/100)
                    it.subtotal=(it.price*it.quantity)-it.descuento
                    it.checkedDescuentoTipoPago=false
                }
                SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)
                updateTotals()
            }
        }
    }
    private fun removeEscalaDiscounts() {
        SharedDataModel.detalleItems.value?.forEach {
            it.porcentajeTotal=it.porcentajeTipoPago
            it.porcentajeEscala = 0.0
            it.descuento=it.price*it.quantity*(it.porcentajeTotal/100)
            it.subtotal=it.price*it.quantity-it.descuento
            it.checkedDescuentoEscala=false
            it.checkedDescuentoTipoPago=false
        }
        SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)
    }
    private fun updateTotals() {
        val items = SharedDataModel.detalleItems.value ?: return
        val total = items.sumOf { it.subtotal }
        view?.findViewById<TextView>(R.id.sumtotal)?.text = "L.${String.format("%.2f", total)}"
        view?.findViewById<TextView>(R.id.sumsubtotal)?.text = "L.${String.format("%.2f", total)}"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_resumen, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewResumen1)
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
            val clientecodigo =  activity?.intent?.getStringExtra("clientecodigo")?:"000"
            val detalleItems = SharedDataModel.detalleItems.value ?: listOf()

            val subtotal = detalleItems.sumOf { it.subtotal }
            val descuento =detalleItems.sumOf { it.descuento } // Calculate any discount if applicable

            val pedidoHdr = PedidoHdr(tipopago = tipoPago, subtotal = subtotal, descuento = descuento, total = subtotal, sinc = false, clientecodigo =clientecodigo )
            val hdrId = DatabaseApplication.getDatabase(requireContext()).PedidoHdrDAO().insertPedidoHdr(pedidoHdr)

            if (hdrId > 0) {
                // Insert was successful, now insert the details
                detalleItems.forEach { item ->
                    val pedidoDtl = PedidoDtl(
                        idhdr = hdrId.toInt(),
                        codigoproducto = item.codigoproducto,
                        cantidad = item.quantity,
                        precio = item.price,
                        descuento = item.descuento,
                        nombre = item.nombreproducto,
                    )
                    DatabaseApplication.getDatabase(requireContext()).PedidoDtlDAO().insertPedidoDtl(pedidoDtl)
                }

                // Clear the SharedDataModel.detalleItems list
                SharedDataModel.detalleItems.postValue(mutableListOf())

                // Go back to the previous fragment on the main thread
                withContext(Dispatchers.Main) {
                    requireActivity().onBackPressed()
                }
            }
        }
    }



}