package com.example.rocacotizacion.DAO

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "AgnAgente")
data class Agente(
    @PrimaryKey @ColumnInfo(name = "idagentes") val idAgentes: Int,
    @ColumnInfo(name = "codigoagentes") val codigoAgentes: String?,
    @ColumnInfo(name = "idsucursal") val idSucursal: Int,
    @ColumnInfo(name = "idtipoagente") val idTipoAgente: Int,
    @ColumnInfo(name = "cuentacontable") val cuentaContable: String?,
    @ColumnInfo(name = "flagpos") val flagPos: Boolean?,
    @ColumnInfo(name = "pctgecomisventa") val pctgeComisVenta: Double?,
    @ColumnInfo(name = "descripcioncorta") val descripcionCorta: String?,
    @ColumnInfo(name = "descripcionlarga") val descripcionLarga: String?,
    @ColumnInfo(name = "usuariocreacion") val usuarioCreacion: String?,
    @ColumnInfo(name = "usuariomodificacion") val usuarioModificacion: String?,
    @ColumnInfo(name = "flagactivo") val flagActivo: Boolean,
    @ColumnInfo(name = "ordenreporte") val ordenReporte: Int,
    @ColumnInfo(name = "username") val username: String?,
    @ColumnInfo(name = "codigoauxiliar") val codigoAuxiliar: String?,
    @ColumnInfo(name = "password") val password: String?,
    @ColumnInfo(name="nombodega") val nombodega: String?,
    @ColumnInfo (name ="rutadesc") val rutadesc : String,
    @ColumnInfo (name ="tipoagente") val tipoagente : String    )
