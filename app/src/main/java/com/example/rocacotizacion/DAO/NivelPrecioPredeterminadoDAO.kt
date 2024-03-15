package com.example.rocacotizacion.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NivelPrecioPredeterminadoDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(NivelPrecioPredeterminado: List<NivelPrecioPredeterminado>)

    @Query("SELECT * FROM NivelPrecioPredeterminado")
    fun getSelectNivelPrecioPredeterminado(): List< NivelPrecioPredeterminado>

    @Query("DELETE FROM NivelPrecioPredeterminado")
     fun deleteAll()
}