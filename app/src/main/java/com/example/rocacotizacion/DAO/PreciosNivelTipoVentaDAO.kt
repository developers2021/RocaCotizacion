package com.example.rocacotizacion.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PreciosNivelTipoVentaDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(PreciosNivelTipoVenta: List<PreciosNivelTipoVenta>)

    @Query("SELECT * FROM PreciosNivelTipoVenta")
    fun getSelectPreciosNivelTipoVenta(): List< PreciosNivelTipoVenta>
}