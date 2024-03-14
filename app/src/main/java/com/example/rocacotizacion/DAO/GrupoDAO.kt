package com.example.rocacotizacion.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GrupoDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(Grupos: List<Grupos>)

    @Query("SELECT * FROM grupos")
    fun getSelectGrupos(): List< Grupos>
}