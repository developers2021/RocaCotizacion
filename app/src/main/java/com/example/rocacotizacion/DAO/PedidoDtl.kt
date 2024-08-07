package com.example.rocacotizacion.DAO

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "pedido_dtl")
data class PedidoDtl(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "idhdr") var idhdr: Int,
    @ColumnInfo(name = "codigoproducto") val codigoproducto: String,
    @ColumnInfo(name = "cantidad") val cantidad: Int,
    @ColumnInfo(name = "precio") val precio: Double,
    @ColumnInfo(name = "descuento") val descuento: Double,
    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "impuesto") val impuesto:Double
)
