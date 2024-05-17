package com.example.rocacotizacion.DataModel

data class PedidoSummary(
    val id: Int,
    val tipopago: String,
    val total: Double,
    val sinc: Boolean,
    val Codigocliente:String
)
