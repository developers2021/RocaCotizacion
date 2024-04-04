package com.example.rocacotizacion.DataModel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DTO.SharedDataModel
import com.example.rocacotizacion.R

class DetallesAdapter(
    var detalles: List<DetalleItem>,
    private val itemCloseClickListener: OnItemCloseClickListener
) : RecyclerView.Adapter<DetallesAdapter.ViewHolder>() {

    interface OnItemCloseClickListener {
        fun onItemCloseClick(position: Int)
    }

    class ViewHolder(view: View, private val itemCloseClickListener: OnItemCloseClickListener) : RecyclerView.ViewHolder(view) {
        val textViewQuantity: TextView = view.findViewById(R.id.textViewQuantity)
        val textViewPrice: TextView = view.findViewById(R.id.textViewPrice)
        val textViewSubtotal: TextView = view.findViewById(R.id.textViewSubtotal)
        private val buttonClose: Button = view.findViewById(R.id.buttonClose)
        private val buttonDecrement: Button = view.findViewById(R.id.buttonDecrement)
        private val buttonIncrement: Button = view.findViewById(R.id.buttonIncrement)

        init {
            buttonClose.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemCloseClickListener.onItemCloseClick(adapterPosition)
                }
            }

            buttonDecrement.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val item = SharedDataModel.detalleItems[adapterPosition]
                    if (item.quantity > 1) {
                        item.quantity -= 1
                        item.subtotal = item.quantity * item.price
                        textViewQuantity.text = item.quantity.toString()
                        textViewSubtotal.text = String.format("L. %.2f", item.subtotal)
                    }
                }
            }

            buttonIncrement.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val item = SharedDataModel.detalleItems[adapterPosition]
                    item.quantity += 1
                    item.subtotal = item.quantity * item.price
                    textViewQuantity.text = item.quantity.toString()
                    textViewSubtotal.text = String.format("L. %.2f", item.subtotal)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.detalle_item, parent, false)
        return ViewHolder(view, itemCloseClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val detalleItem = detalles[position]
        holder.textViewQuantity.text = detalleItem.quantity.toString()
        holder.textViewPrice.text = String.format("L. %.2f", detalleItem.price)
        holder.textViewSubtotal.text = String.format("L. %.2f", detalleItem.subtotal)
    }

    override fun getItemCount(): Int = detalles.size

    fun updateDetalles(newDetalles: List<DetalleItem>) {
        detalles = newDetalles
        notifyDataSetChanged()
    }
}
