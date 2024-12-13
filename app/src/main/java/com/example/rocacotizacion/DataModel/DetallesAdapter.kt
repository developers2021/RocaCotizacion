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
    private var detalles: MutableList<DetalleItem>,
    private val itemCloseClickListener: OnItemCloseClickListener
) : RecyclerView.Adapter<DetallesAdapter.ViewHolder>() {

    interface OnItemCloseClickListener {
        fun onItemCloseClick(position: Int)
    }

    // ViewHolder como clase interna para acceder a 'detalles' actualizado
    inner class ViewHolder(
        view: View,
        private val itemCloseClickListener: OnItemCloseClickListener
    ) : RecyclerView.ViewHolder(view) {

        val textViewQuantity: TextView = view.findViewById(R.id.textViewQuantity)
        val textViewPrice: TextView = view.findViewById(R.id.textViewPrice)
        val textViewSubtotal: TextView = view.findViewById(R.id.textViewSubtotal)
        val textViewImpuesto: TextView = view.findViewById(R.id.textViewImpuesto)
        val textViewTotal: TextView = view.findViewById(R.id.textViewTotal)
        val textViewDescuento: TextView = view.findViewById(R.id.textViewDescuento)
        val textViewNombreProd: TextView = view.findViewById(R.id.textViewNomProd)
        val buttonClose: TextView = view.findViewById(R.id.buttonClose)
        val buttonDecrement: Button = view.findViewById(R.id.buttonDecrement)
        val buttonIncrement: Button = view.findViewById(R.id.buttonIncrement)

        init {
            buttonClose.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemCloseClickListener.onItemCloseClick(adapterPosition)
                }
            }

            buttonDecrement.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    updateQuantity(adapterPosition, decrement = true)
                }
            }

            buttonIncrement.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    updateQuantity(adapterPosition, decrement = false)
                }
            }
        }

        private fun updateQuantity(position: Int, decrement: Boolean) {
            val item = detalles[position]
            if (decrement && item.quantity > 1) {
                item.quantity -= 1
            } else if (!decrement) {
                item.quantity += 1
            }

            // Recalcular valores después del cambio
            item.subtotal = item.price * item.quantity
            item.descuento = item.subtotal * (item.porcentajeTotal / 100)
            item.valorimpuesto = (item.subtotal - item.descuento) * (item.porcentajeImpuesto / 100)
            item.total = item.subtotal - item.descuento + item.valorimpuesto

            // Notificar cambios al estado compartido
            SharedDataModel.detalleItems.postValue(detalles.toMutableList())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.detalle_item, parent, false)
        return ViewHolder(view, itemCloseClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val symbols = DecimalFormatSymbols(Locale("es", "HN")).apply {
            currencySymbol = "L"
            decimalSeparator = '.'
            groupingSeparator = ','
        }
        val customFormat = DecimalFormat("¤#,##0.00", symbols)
        val detalleItem = detalles[position]
        holder.textViewQuantity.text = detalleItem.quantity.toString()
        holder.textViewPrice.text = customFormat.format(detalleItem.price)
        holder.textViewSubtotal.text = customFormat.format(detalleItem.subtotal)
        holder.textViewImpuesto.text = customFormat.format(detalleItem.valorimpuesto)
        holder.textViewNombreProd.text = detalleItem.nombreproducto
        holder.textViewDescuento.text = customFormat.format(detalleItem.descuento)
        holder.textViewTotal.text = customFormat.format(detalleItem.total)

        holder.buttonIncrement.isEnabled = detalleItem.isEnabled ?: true
        holder.buttonDecrement.isEnabled = detalleItem.isEnabled ?: true
        holder.buttonClose.isEnabled = detalleItem.isEnabled ?: true
    }

    override fun getItemCount(): Int = detalles.size

    fun updateDetalles(newDetalles: List<DetalleItem>) {
        detalles.clear()
        detalles.addAll(newDetalles)
        notifyDataSetChanged()
    }
}
