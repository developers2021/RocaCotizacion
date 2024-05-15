package com.example.rocacotizacion.ui.MiDia

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.DAO.PedidoDtl
import com.example.rocacotizacion.DAO.PedidoHdr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PedidoViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DatabaseApplication.getDatabase(application)
    private val pedidoHdrDAO = database.PedidoHdrDAO()
    private val pedidoDtlDAO = database.PedidoDtlDAO()

    val pedidoHdrList: LiveData<List<PedidoHdr>> = pedidoHdrDAO.getAllPedidoHdr()

    fun getPedidoHdrById(pedidoId: Int): LiveData<PedidoHdr> {
        return pedidoHdrDAO.getPedidoHdrById(pedidoId)
    }

    fun getDetailsByPedidoId(pedidoId: Int): LiveData<List<PedidoDtl>> {
        return pedidoDtlDAO.getDetailsByPedidoId(pedidoId)
    }

    fun getDetalleTotal(idhdr: Int): LiveData<Double> {
        return pedidoDtlDAO.getSumCantidadPrecioByHdrId(idhdr)
    }

    fun insertPedido(pedidoHdr: PedidoHdr, pedidoDtlList: List<PedidoDtl>) {
        viewModelScope.launch(Dispatchers.IO) {
            val hdrId = pedidoHdrDAO.insertPedidoHdr(pedidoHdr)
            if (hdrId > 0) {
                pedidoDtlList.forEach { detalle ->
                    detalle.idhdr = hdrId.toInt()
                    pedidoDtlDAO.insertPedidoDtl(detalle)
                }
            }
        }
    }
}
