package com.example.rocacotizacion.DAO

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invdescuentoportipoventa")
data class invdescuentoportipoventa(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "idtipoventa") val idtipoventa: Int,
    @ColumnInfo(name = "idpromocion") val idpromocion: Int,
    @ColumnInfo(name = "promocion") val promocion: String,
    @ColumnInfo(name = "codigotipopromocion") val codigotipopromocion: String,
    @ColumnInfo(name = "tipopromocion") val tipopromocion: String,
    @ColumnInfo(name = "idproducto") val idproducto: Int,
    @ColumnInfo(name = "monto") val monto: Double,
    @ColumnInfo(name = "codigoproducto") val codigoproducto: String,
    @ColumnInfo(name = "codigotipoventa") val codigotipoventa: String

)
