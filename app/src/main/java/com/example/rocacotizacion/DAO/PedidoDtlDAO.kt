package com.example.rocacotizacion.DAO

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PedidoDtlDAO {
    @Insert
    fun insertPedidoDtl(pedidoDtl: PedidoDtl)
    @Query("SELECT SUM(cantidad * precio) FROM pedido_dtl WHERE idhdr = :idhdr")
    fun getSumCantidadPrecioByHdrId(idhdr: Int): LiveData<Double>
    @Query("DELETE FROM pedido_dtl")
    fun deleteAll()
}
