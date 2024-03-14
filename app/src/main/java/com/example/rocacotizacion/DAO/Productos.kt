package com.example.rocacotizacion.DAO

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Productos")
data class Productos(
    @PrimaryKey @ColumnInfo(name = "idproducto") val idproducto: Int,
    @ColumnInfo(name = "codigoproducto") val codigoproducto: String?,
    @ColumnInfo(name = "producto") val producto: String?,
    @ColumnInfo(name = "idgrupo") val idgrupo: Int,
    @ColumnInfo(name = "grupo") val grupo: String?,
    @ColumnInfo(name = "idtipoproducto") val idtipoproducto: Int,
    @ColumnInfo(name = "costoactual") val costoactual: Double,
    @ColumnInfo(name = "idimpuesto") val idimpuesto: Int,
    @ColumnInfo(name = "porcentajeimpuesto") val porcentajeimpuesto: Double,
    @ColumnInfo(name = "precio") val precio: Double?,
    @ColumnInfo(name = "descuento") val descuento: Double?
)
