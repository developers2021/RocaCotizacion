package com.example.rocacotizacion.DataModel

data class PedidoPrintModel(
    var pedidoId: Int,
    var fechaEmision: String,
    var tipoventa: String,
    var clientenombre: String,
    var codigocliente: String,
    var rtncliente: String,
    var rutanombre: String,
    var vendedornombre: String,
)
