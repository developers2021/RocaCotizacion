package com.example.rocacotizacion.ui.DetallePedido

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DAO.PedidoDtl
import com.example.rocacotizacion.R
import android.util.Log
class PedidoDetailAdapter(private var details: List<PedidoDtl>) : RecyclerView.Adapter<PedidoDetailAdapter.DetailViewHolder>() {

    class DetailViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val codigoProducto: TextView = view.findViewById(R.id.codigoProducto)
        private val cantidad: TextView = view.findViewById(R.id.cantidad)
        private val precio: TextView = view.findViewById(R.id.precio)

        fun bind(detail: PedidoDtl) {
            codigoProducto.text = detail.codigoproducto
            cantidad.text = detail.cantidad.toString()
            precio.text = String.format("%.2f", detail.precio)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pedido_detail, parent, false)
        return DetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.bind(details[position])
    }

    override fun getItemCount(): Int = details.size


    fun updateDetails(newDetails: List<PedidoDtl>) {
        details = newDetails
        notifyDataSetChanged()
    }
}
