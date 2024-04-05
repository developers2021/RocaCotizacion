package com.example.rocacotizacion.DataModel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DTO.SharedDataModel
import com.example.rocacotizacion.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

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
        val textViewNombreProd:TextView=view.findViewById(R.id.textViewNomProd)
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
                    SharedDataModel.detalleItems.value?.let { items ->
                        val item = items[adapterPosition]
                        if (item.quantity > 1) {
                            item.quantity -= 1
                            item.subtotal = item.quantity * item.price
                            SharedDataModel.detalleItems.postValue(items)
                        }
                    }
                }
            }

            buttonIncrement.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    SharedDataModel.detalleItems.value?.let { items ->
                        val item = items[adapterPosition]
                        item.quantity += 1
                        item.subtotal = item.quantity * item.price
                        SharedDataModel.detalleItems.postValue(items)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.detalle_item, parent, false)
        return ViewHolder(view, itemCloseClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val symbols = DecimalFormatSymbols(Locale("es", "HN")).apply {
            currencySymbol = "L." 
            decimalSeparator = '.'
            groupingSeparator = ','
        }
        val customFormat = DecimalFormat("¤#,##0.00", symbols)
        val detalleItem = detalles[position]
        holder.textViewQuantity.text = detalleItem.quantity.toString()
        holder.textViewPrice.text = customFormat.format(detalleItem.price)
        holder.textViewSubtotal.text = customFormat.format(detalleItem.subtotal)
        holder.textViewNombreProd.text=detalleItem.nombreproducto
    }

    override fun getItemCount(): Int = detalles.size

    fun updateDetalles(newDetalles: List<DetalleItem>) {
        detalles = newDetalles
        notifyDataSetChanged()
    }
}
