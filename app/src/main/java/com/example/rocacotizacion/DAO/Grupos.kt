package com.example.rocacotizacion.DAO

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Grupos")
data class Grupos(
    @PrimaryKey @ColumnInfo(name = "idgrupo") val idgrupo: Int,
    @ColumnInfo(name = "grupo") val grupo: String?
)
