
package com.example.rocacotizacion.DataModel

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.DTO.SharedDataModel
import com.example.rocacotizacion.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        val textViewImpuesto: TextView = view.findViewById(R.id.textViewImpuesto)
        val textViewNombreProd:TextView=view.findViewById(R.id.textViewNomProd)
        val buttonClose: Button = view.findViewById(R.id.buttonClose)
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
        // A method to fetch and recalculate discounts based on the new quantity
        private fun recalculateDiscounts(item: DetalleItem, isEscalaEnabled: Boolean) {
            CoroutineScope(Dispatchers.IO).launch {
                // Conditionally apply escala discount if the corresponding switch is on
                if (isEscalaEnabled) {
                    val escalaDiscounts = DatabaseApplication.getDatabase(itemView.context)
                        .invdescuentoporescalaDAO()
                        .getDescuentoPorEscala(item.codigoproducto)
                    val escalaDiscount = escalaDiscounts.firstOrNull {
                        // Check if the quantity falls within the range
                        item.quantity >= it.rangoinicial && item.quantity <= it.rangofinal
                    }
                    item.porcentajeEscala=escalaDiscount?.monto ?: 0.00
                    item.porcentajeTotal=item.porcentajeEscala+item.porcentajeTipoPago+item.porcentajeRuta
                    item.descuento=(item.price*item.quantity)*(item.porcentajeTotal/100)
                    item.subtotal=(item.price*item.quantity)-item.descuento
                    item.valorimpuesto=item.subtotal*(item.porcentajeImpuesto/100)
                    item.total=item.subtotal+item.valorimpuesto
                } else {
                    item.descuento=(item.price*item.quantity)*((item.porcentajeTipoPago/100)+(item.porcentajeRuta/100))
                }
                SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)


                withContext(Dispatchers.Main) {
                    // Update subtotal after recalculating discounts
                    //             impuesto es despues del descuento
                    item.subtotal = item.quantity * item.price -(item.quantity * item.price *( item.porcentajeTotal/100))
                    item.valorimpuesto=((item.price*item.quantity)-(item.price*item.quantity*(item.porcentajeTotal/100)))*(item.porcentajeImpuesto/100)
                    item.total=(item.quantity * item.price -(item.quantity * item.price *( item.porcentajeTotal/100)))+(((item.price*item.quantity)-(item.price*item.quantity*(item.porcentajeTotal/100)))*(item.porcentajeImpuesto/100))
                    // Post the updated list to LiveData to trigger observers
                    SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)
                }
            }
        }
        private fun updateQuantity(position: Int, decrement: Boolean) {
            SharedDataModel.detalleItems.value?.let { items ->
                val item = items[position]
                if (decrement && item.quantity > 1) {
                    item.quantity -= 1
                } else if (!decrement) {
                    item.quantity += 1
                }
                recalculateDiscounts(item, item.checkedDescuentoEscala)

                // Recalculate subtotal after discounts are applied
               // item.subtotal = item.quantity * item.price - item.descuentoEscala - item.descuentoTipoPago

                // Post the updated list to LiveData to trigger observers
                SharedDataModel.detalleItems.postValue(items)
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
        holder.textViewImpuesto.text = customFormat.format(detalleItem.valorimpuesto)
        holder.textViewNombreProd.text=detalleItem.nombreproducto
        //evalua si el pedido aun no fue guardado en la base de datos, para habilitar o deshabilitar los botones, si es nulo se habilita todos los botones
        holder.buttonIncrement.isEnabled = detalleItem.isEnabled?:true
        holder.buttonDecrement.isEnabled = detalleItem.isEnabled?:true
        holder.buttonClose.isEnabled = detalleItem.isEnabled?:true
    }

    override fun getItemCount(): Int = detalles.size

    fun updateDetalles(newDetalles: List<DetalleItem>) {
        detalles = newDetalles
        notifyDataSetChanged()
    }
}