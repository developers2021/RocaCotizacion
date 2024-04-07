package com.example.rocacotizacion.DAO

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface PedidoDtlDAO {
    @Insert
    fun insertPedidoDtl(pedidoDtl: PedidoDtl)

}
