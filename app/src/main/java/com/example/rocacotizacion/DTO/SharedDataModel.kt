package com.example.rocacotizacion.DTO

import androidx.lifecycle.MutableLiveData
import com.example.rocacotizacion.DataModel.DetalleItem

object SharedDataModel {
    var detalleItems: MutableLiveData<MutableList<DetalleItem>> = MutableLiveData(mutableListOf())

    fun getResumenText(): String {
        // Build your resumen text based on the detalleItems or other data
        val resumen = StringBuilder()
        detalleItems.value?.forEach { item ->
            resumen.append("${item.quantity} x ${item.price} = L. ${item.subtotal}\n")
        }
        // Add more details as needed
        return resumen.toString()
    }
}