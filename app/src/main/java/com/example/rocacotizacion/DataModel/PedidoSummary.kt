package com.example.rocacotizacion.DataModel

data class PedidoSummary(
    val id: Int,
    val codigopedido: String,
    val tipopago: String,
    val total: Double,
    val sinc: Boolean,
    val Codigocliente:String,
    val anulado: String // Nuevo campo que indica si está anulado
)
