package com.example.rocacotizacion.ui.QuantityProd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DataModel.DiscountItem
import com.example.rocacotizacion.R

class DiscountsAdapter(
    private val descuentos: List<DiscountItem>
) : RecyclerView.Adapter<DiscountsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvType: TextView = view.findViewById(R.id.tvDiscountType)
        val tvPromotionName: TextView = view.findViewById(R.id.tvPromotionName)
        val discountContainer: LinearLayout = view.findViewById(R.id.discountContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_discount, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val discount = descuentos[position]

        when (discount.type) {
            "Escala" -> {
                holder.tvType.text = "Escala"
                holder.tvPromotionName.text = discount.promocion

                // Limpiar contenedor dinámico
                holder.discountContainer.removeAllViews()

                // Dividir rangos de unidades y descuentos
                val rangos = discount.rangoUnidades.split("\n")
                rangos.forEach { rango ->
                    val rangoSplit = rango.split(":")
                    if (rangoSplit.size == 2) {
                        val rangoUnidades = rangoSplit[0].trim()
                        val descuento = rangoSplit[1].trim()

                        // Crear una nueva fila dinámica
                        val row = LinearLayout(holder.itemView.context).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 4, 0, 4)
                            }
                            gravity = android.view.Gravity.CENTER
                        }

                        // Columna para Rango de Unidades
                        val rangoText = TextView(holder.itemView.context).apply {
                            text = rangoUnidades
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                            textSize = 14f
                            gravity = android.view.Gravity.CENTER
                            setTextColor(holder.itemView.context.getColor(R.color.textPrimary))
                        }

                        // Columna para el Descuento
                        val descuentoText = TextView(holder.itemView.context).apply {
                            text = descuento
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                            textSize = 14f
                            gravity = android.view.Gravity.CENTER
                            setTextColor(holder.itemView.context.getColor(R.color.textHighlight))
                        }

                        // Agregar columnas a la fila
                        row.addView(rangoText)
                        row.addView(descuentoText)

                        // Agregar fila al contenedor dinámico
                        holder.discountContainer.addView(row)
                    }
                }
            }

            "Tipo de Venta" -> {
                holder.tvType.text = "Tipo de Venta"
                holder.tvPromotionName.text = discount.promocion

                // Limpiar contenedor dinámico
                holder.discountContainer.removeAllViews()

                // Mostrar solo el descuento de tipo de venta
                val descuentoText = TextView(holder.itemView.context).apply {
                    text = "Descuento: ${discount.monto}%"
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    textSize = 14f
                    gravity = android.view.Gravity.CENTER
                    setTextColor(holder.itemView.context.getColor(R.color.textHighlight))
                }

                // Agregar al contenedor dinámico
                holder.discountContainer.addView(descuentoText)
            }

            else -> {
                // Default para otros tipos de descuentos
                holder.tvType.text = discount.type
                holder.tvPromotionName.text = discount.promocion
                holder.discountContainer.removeAllViews()
            }
        }
    }

    override fun getItemCount(): Int = descuentos.size
}
