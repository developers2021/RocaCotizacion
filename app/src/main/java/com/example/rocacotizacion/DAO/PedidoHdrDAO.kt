package com.example.rocacotizacion.DAO

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PedidoHdrDAO {
    @Insert
    fun insertPedidoHdr(pedidoHdr: PedidoHdr): Long
    @Query("SELECT * FROM pedido_hdr")
    fun getAllPedidoHdr(): LiveData<List<PedidoHdr>>
    @Query("DELETE FROM pedido_hdr")
    fun deleteAll()
    @Query("SELECT * FROM pedido_hdr WHERE id = :id")
    fun getPedidoHdrById(id: Int): LiveData<PedidoHdr>

    @Query("DELETE FROM pedido_hdr where id=:id")
    fun deletehdrid(id:Int)
}
