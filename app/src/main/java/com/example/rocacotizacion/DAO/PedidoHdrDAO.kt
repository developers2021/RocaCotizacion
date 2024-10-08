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
    @Query("SELECT * FROM pedido_hdr WHERE id = :id")
    fun getPedidoPrinteById(id: Int): PedidoHdr
    @Query("DELETE FROM pedido_hdr where id=:id")
    fun deletehdrid(id:Int)
    @Query("SELECT COUNT(*) FROM pedido_hdr")
    fun countAll(): Int

    @Query("SELECT * FROM pedido_hdr where sinc=false")
    fun getAllPedidoHdrS(): List<PedidoHdr>
    @Query("UPDATE pedido_hdr SET sinc = :sinc WHERE id IN (:ids)")
    fun updateSincForIds(ids: List<Int>, sinc: Boolean)
    @Query("SELECT sinc FROM pedido_hdr WHERE id = :pedidoId")
    fun getSincByPedidoId(pedidoId: Int): LiveData<Boolean?>
}
