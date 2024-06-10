package com.example.rocacotizacion.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AgenteDAO {
    @Insert
    fun insert(agente: Agente)

    @Query("SELECT * FROM AgnAgente WHERE username = :username")
    fun getAgenteByUsername(username: String): Agente?

    @Query("DELETE FROM AgnAgente")
     fun deleteAll()

    @Query("SELECT * FROM AgnAgente")
    fun getAgente(): Agente
}