package com.example.rocacotizacion.DTO

import androidx.lifecycle.MutableLiveData
import com.example.rocacotizacion.DataModel.DetalleItem
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

object SharedDataModel {
    var detalleItems: MutableLiveData<MutableList<DetalleItem>> = MutableLiveData(mutableListOf())
}