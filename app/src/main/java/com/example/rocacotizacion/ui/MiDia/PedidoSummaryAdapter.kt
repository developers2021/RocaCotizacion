package com.example.rocacotizacion.ui.MiDia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DataModel.PedidoSummary
import com.example.rocacotizacion.R

class PedidoSummaryAdapter(private var items: List<PedidoSummary>) : RecyclerView.Adapter<PedidoSummaryAdapter.PedidoSummaryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoSummaryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pedido_summary, parent, false)
        return PedidoSummaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoSummaryViewHolder, position: Int) {
        val item = items[position]
        holder.tvPedidoId.text = "Pedido ID: ${item.id}"
        holder.tvTipopago.text = "Tipo de pago: ${item.tipopago}"
        holder.tvTotal.text = "Total: ${String.format("%.2f", item.total)}"
    }

    override fun getItemCount(): Int = items.size

    class PedidoSummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPedidoId: TextView = itemView.findViewById(R.id.tvPedidoId)
        val tvTipopago: TextView = itemView.findViewById(R.id.tvTipopago)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotal)
    }
    fun updateItems(newItems: List<PedidoSummary>) {
        items = newItems
        notifyDataSetChanged()
    }
}
