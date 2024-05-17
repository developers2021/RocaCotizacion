package com.example.rocacotizacion.DataModel

data class SendPedido (
    val idAgentes: Int,
    val codigoAgentes: String?,
    val idSucursal: Int,
    val idBodega: String,
    val pedidos: List<PedidoHdrS>
)

data class PedidoHdrS(
    val id: Int = 0,
    val tipoPago: String,
    val subtotal: Double,
    val clientecodigo:String,
    val descuento: Double? = null,
    val total: Double,
    val isSincronizado: Boolean,
    val pedidoDtls: List<PedidoDtlS>
)

data class PedidoDtlS(
    val id: Int = 0,
    val idhdr: Int,
    val codigoProducto: String,
    val cantidad: Int,
    val precio: Double,
    val descuento: Double? = null
)
