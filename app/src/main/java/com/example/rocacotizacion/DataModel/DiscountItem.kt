package com.example.rocacotizacion.DataModel

data class DiscountItem(
    val type: String,             // Tipo de descuento (Escala, Ruta, Tipo de Venta)
    val rangoUnidades: String = "", // Rango de unidades (solo para descuentos por escala)
    val monto: Double,            // Monto o porcentaje del descuento
    val promocion: String,        // Nombre o descripción de la promoción
    val fechaInicio: String = "-", // Fecha de inicio del descuento
    val fechaFin: String = "-"     // Fecha de fin del descuento
)
