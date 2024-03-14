package com.example.rocacotizacion.DAO

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PreciosNivelTipoVenta")
data class PreciosNivelTipoVenta(
    @PrimaryKey @ColumnInfo(name = "idproducto") val idproducto: Int,
    @ColumnInfo(name = "precio") val precio: Double,
    @ColumnInfo(name = "idnivelprecio") val idnivelprecio: Int,
    @ColumnInfo(name = "idtipoventa") val idtipoventa: Int,
    @ColumnInfo(name = "nivelprecio") val nivelprecio: String,
    @ColumnInfo(name = "tipoventa") val tipoventa: String,
    @ColumnInfo(name = "codigotipoventa") val codigotipoventa: String )
