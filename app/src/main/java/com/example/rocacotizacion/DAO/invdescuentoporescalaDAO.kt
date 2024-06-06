package com.example.rocacotizacion.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface invdescuentoporescalaDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(model: List<invdescuentoporescala>)

    @Query("DELETE FROM invdescuentoporescala")
    fun deleteAll()

    @Query("SELECT * FROM invdescuentoporescala WHERE codigoproducto = :codigoproducto LIMIT 1")
    fun getDescuentoPorEscala(codigoproducto: String): List<invdescuentoporescala>
}