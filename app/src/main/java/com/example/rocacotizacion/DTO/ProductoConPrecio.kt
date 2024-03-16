package com.example.rocacotizacion.DTO

data class ProductoConPrecio(
    val idproducto: Int,
    val codigoproducto: String?,
    val producto: String?,
    val idgrupo: Int,
    val grupo: String?,
    val idtipoproducto: Int,
    val costoactual: Double,
    val idimpuesto: Int,
    val porcentajeimpuesto: Double,
    val precio: Double?,
    val descuento: Double?
)
