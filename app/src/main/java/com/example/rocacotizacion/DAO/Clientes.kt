package com.example.rocacotizacion.DAO

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "Clientes")
data class Clientes(
    @PrimaryKey @ColumnInfo(name = "idcliente") val idcliente: Int,
    @ColumnInfo(name = "nombrecliente") val nombrecliente: String?,
    @ColumnInfo(name = "Codigocliente") val Codigocliente: String?,
    @ColumnInfo(name = "Rtncliente") val Rtncliente: String? )
