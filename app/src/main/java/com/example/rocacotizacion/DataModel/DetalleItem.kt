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
    var checkedDescuentoRuta: Boolean = false,
    var descuentoTotal: Double = 0.0,
    var porcentajeEscala: Double = 0.0,
    var porcentajeTipoPago: Double = 0.0,
    var porcentajeRuta: Double = 0.0,
    var porcentajeTotal: Double = 0.0,
    var isEnabled: Boolean = true,
    val porcentajeImpuesto: Double = 0.0,
    var valorimpuesto: Double = 0.0,
    var total: Double = 0.0
)
