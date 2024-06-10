package com.example.rocacotizacion.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClientesDAO {
    //@Insert
    //fun insert(clientes: Clientes)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertAll(clientes: List<Clientes>)

    @Query("SELECT * FROM clientes")
    fun getSelectClientes(): List< Clientes>

    @Query("DELETE FROM clientes")
     fun deleteAll()
    @Query("SELECT * FROM Clientes WHERE Codigocliente = :codcliente")
    fun getClientById(codcliente: String): Clientes
}