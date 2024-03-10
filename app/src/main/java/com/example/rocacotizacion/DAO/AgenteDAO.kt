package com.example.rocacotizacion.DAO

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AgenteDAO {
    @Insert
    fun insert(agente: Agente)

    @Query("SELECT * FROM AgnAgente WHERE username = :username")
    fun getAgenteByUsername(username: String): Agente?
}