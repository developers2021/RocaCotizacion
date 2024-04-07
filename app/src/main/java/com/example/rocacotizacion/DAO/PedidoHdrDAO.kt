package com.example.rocacotizacion.DAO

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface PedidoHdrDAO {
    @Insert
    fun insertPedidoHdr(pedidoHdr: PedidoHdr): Long

}
