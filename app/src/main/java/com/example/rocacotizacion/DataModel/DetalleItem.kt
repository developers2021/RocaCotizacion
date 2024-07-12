package com.example.rocacotizacion.DataModel

data class DetalleItem(
    var quantity: Int,
    var price: Double,
    var subtotal: Double,
    var nombreproducto: String,
    var codigoproducto:String,
    var descuento: Double = 0.0,
    var checkedDescuentoEscala: Boolean = false,
    var checkedDescuentoTipoPago: Boolean = false,
    var porcentajeEscala: Double = 0.0,
    var porcentajeTipoPago: Double = 0.0,
    var porcentajeTotal: Double = 0.0,
    var isEnabled: Boolean = true,
)
