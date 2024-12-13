package com.example.rocacotizacion.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rocacotizacion.DAO.Clientes

@Dao
interface ClientesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(clientes: List<Clientes>)

    @Query("SELECT * FROM Clientes")
    fun getSelectClientes(): List<Clientes>

    @Query("DELETE FROM Clientes")
    fun deleteAll()

    @Query("SELECT * FROM Clientes WHERE Codigocliente = :codcliente")
    fun getClientById(codcliente: String): Clientes

    @Query("SELECT * FROM Clientes WHERE Codigocliente = :codigoCliente LIMIT 1")
    fun obtenerClientePorCodigo(codigoCliente: String): Clientes?
}
