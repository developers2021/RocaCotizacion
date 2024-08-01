package com.example.rocacotizacion.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface invdescuentoportipoventaDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(model: List<invdescuentoportipoventa>)

    @Query("DELETE FROM invdescuentoportipoventa")
    fun deleteAll()
    @Query("SELECT * FROM invdescuentoportipoventa WHERE codigoproducto = :codigoproducto AND codigotipoventa = :codigotipoventa LIMIT 1")
    fun getDescuentoPorTipoVenta(codigoproducto: String,codigotipoventa :String): invdescuentoportipoventa?
}