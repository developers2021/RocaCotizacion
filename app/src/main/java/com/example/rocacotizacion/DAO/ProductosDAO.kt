package com.example.rocacotizacion.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
@Dao
interface ProductosDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(productos: MutableList<Productos>)
    @Query("SELECT * FROM productos")
    fun getProductos(): List<Productos>

    @Query("DELETE FROM productos")
     fun deleteAll()
}