package com.example.rocacotizacion.DAO

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pedido_hdr")
data class PedidoHdr(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "tipopago") val tipopago: String,
    @ColumnInfo(name = "subtotal") val subtotal: Double,
    @ColumnInfo(name = "descuento") val descuento: Double,
    @ColumnInfo(name = "impuesto") val impuesto: Double,
    @ColumnInfo(name = "total") val total: Double,
    @ColumnInfo(name="sinc") val sinc:Boolean,
    @ColumnInfo(name="clientecodigo") val clientecodigo:String
)
