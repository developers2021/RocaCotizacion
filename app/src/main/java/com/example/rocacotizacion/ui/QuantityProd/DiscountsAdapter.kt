package com.example.rocacotizacion.ui.QuantityProd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.R

data class DiscountItem(
    val type: String,
    val porcentaje: Double
)

class DiscountsAdapter(
    private val descuentos: List<DiscountItem>
) : RecyclerView.Adapter<DiscountsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvType: TextView = view.findViewById(R.id.tvDiscountType)
        val tvPorcentaje: TextView = view.findViewById(R.id.tvDiscountPercentage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_discount, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val discount = descuentos[position]
        holder.tvType.text = discount.type
        holder.tvPorcentaje.text = "${discount.porcentaje}%"
    }

    override fun getItemCount(): Int = descuentos.size
}
