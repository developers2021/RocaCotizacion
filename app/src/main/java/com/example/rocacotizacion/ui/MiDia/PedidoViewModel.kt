package com.example.rocacotizacion.ui.MiDia

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.rocacotizacion.DAO.Agente
import com.example.rocacotizacion.DAO.Clientes
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.DAO.PedidoDtl
import com.example.rocacotizacion.DAO.PedidoHdr
import com.example.rocacotizacion.DataModel.PedidoPrintModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import android.content.Context


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



}
