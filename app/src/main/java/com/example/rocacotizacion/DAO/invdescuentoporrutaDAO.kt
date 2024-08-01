package com.example.rocacotizacion.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface invdescuentoporrutaDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(model: List<invdescuentoporruta>)

    @Query("DELETE FROM invdescuentoporruta")
    fun deleteAll()
    @Query("SELECT * FROM invdescuentoporruta WHERE idruta   = :idruta LIMIT 1")
    fun getDescuentoPorRuta(idruta: Int): invdescuentoporruta?

    @Query("SELECT * FROM invdescuentoporruta LIMIT 1")
    fun getDescuentoPorRutafirst(): invdescuentoporruta?
}