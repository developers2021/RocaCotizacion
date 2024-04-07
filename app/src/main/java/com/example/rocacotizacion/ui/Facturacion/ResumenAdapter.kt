package com.example.rocacotizacion.ui.Facturacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DataModel.DetalleItem
import com.example.rocacotizacion.R

class ResumenAdapter(private var items: List<DetalleItem>) : RecyclerView.Adapter<ResumenAdapter.ResumenViewHolder>() {

    fun updateItems(newItems: List<DetalleItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResumenViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.resumen_item, parent, false)
        return ResumenViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResumenViewHolder, position: Int) {
        val item = items[position]
        holder.textViewProduct.text = if (item.nombreproducto.length > 25) "${item.nombreproducto.take(25)}..." else item.nombreproducto
        holder.textViewPrice.text = "${item.quantity} X L.${item.price}"
        holder.textViewSubtotal.text = "L.${item.subtotal}"
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ResumenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewProduct: TextView = itemView.findViewById(R.id.textViewProduct)
        val textViewPrice: TextView = itemView.findViewById(R.id.textViewPrice)
        val textViewSubtotal: TextView = itemView.findViewById(R.id.textViewSubtotal)
    }
}


