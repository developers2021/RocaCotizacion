package com.example.rocacotizacion.ui.Facturacion

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DTO.SharedDataModel
import com.example.rocacotizacion.DataModel.DetalleItem
import com.example.rocacotizacion.R
import java.text.DecimalFormat


class ResumenAdapter(private var items: List<DetalleItem>) : RecyclerView.Adapter<ResumenAdapter.ResumenViewHolder>() {

    fun updateItems(newItems: List<DetalleItem>) {
        items = newItems
        notifyDataSetChanged()
    }
    fun formatNumber(value: Double?): String {
        val formatter = DecimalFormat("#,##0.00")
        return formatter.format(value ?: 0.0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResumenViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.resumen_item, parent, false)
        return ResumenViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResumenViewHolder, position: Int) {
        val item = items[position]

        holder.textViewProduct.text = if (item.nombreproducto.length > 25) {
            "${item.nombreproducto.take(25)}..."
        } else {
            item.nombreproducto
        }

        // Formatear cantidad y precio
        val formattedPrice = formatNumber(item.price)
        val formattedSubtotal = formatNumber(item.subtotal)

        // Mostrar los valores formateados
        holder.textViewPrice.text = "${item.quantity} X L. $formattedPrice"
        holder.textViewSubtotal.text = "L. $formattedSubtotal"
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


