package com.example.rocacotizacion.DTO

import androidx.lifecycle.MutableLiveData
import com.example.rocacotizacion.DataModel.DetalleItem
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

object SharedDataModel {
    var detalleItems: MutableLiveData<MutableList<DetalleItem>> = MutableLiveData(mutableListOf())


    fun getprice(): String {
        val resumen = StringBuilder()
        detalleItems.value?.forEach { item ->
            resumen.append("${item.quantity}X L.${item.price}\n\n")
        }
        return resumen.toString()
    }
    fun getTotalPrice(): String {
        var total = 0.0 // This will hold the sum of quantity times price
        detalleItems.value?.forEach { item ->
            total += item.quantity * item.price
        }
        val format = NumberFormat.getCurrencyInstance(Locale("es", "HN")) // Set the appropriate Locale
        return format.format(total) // Format and return the total as a currency string
    }

    fun getsubtotal(): String {
        val resumen = StringBuilder()
        val symbols = DecimalFormatSymbols().apply {
            currencySymbol = "L." // Set custom currency symbol
            groupingSeparator = ',' // Set grouping separator like comma, dot, or space
            decimalSeparator = '.' // Set decimal separator
        }
        val format = DecimalFormat("¤#,##0.00", symbols) // ¤ is the placeholder for currency symbol
        detalleItems.value?.forEach { item ->
            resumen.append("${format.format(item.subtotal)}\n\n")
        }
        return resumen.toString()
    }
    fun getproducto(): String {
        val resumen = StringBuilder()
        detalleItems.value?.forEach { item ->
            if (item.nombreproducto.length>25)
                resumen.append("${item.nombreproducto.take(25)}...\n\n")
            else
                resumen.append("${item.nombreproducto.take(25)}\n\n")

        }
        return resumen.toString()
    }
}