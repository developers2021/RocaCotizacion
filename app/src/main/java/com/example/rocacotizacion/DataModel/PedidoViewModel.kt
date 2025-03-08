package com.example.rocacotizacion.DataModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.rocacotizacion.DAO.PedidoHdr
import com.example.rocacotizacion.DAO.PedidoDtl
import com.example.rocacotizacion.DAO.DatabaseApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PedidoViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData para el encabezado y detalles del pedido
    val pedidoHdrLiveData = MutableLiveData<PedidoHdr>()
    val pedidoDtlLiveData = MutableLiveData<List<PedidoDtl>>()

    /**
     * Carga el pedido existente desde la base de datos dado un pedidoId.
     * Se asume que:
     * - getPedidoPrinteById() devuelve el encabezado del pedido de forma síncrona.
     * - getAllDetailsByHeaderId() devuelve la lista de detalles del pedido.
     */
    fun loadPedido(pedidoId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = DatabaseApplication.getDatabase(getApplication())
            try {
                // Cargar el encabezado del pedido
                val hdr: PedidoHdr = db.PedidoHdrDAO().getPedidoPrinteById(pedidoId)
                // Cargar los detalles del pedido
                val details: List<PedidoDtl> = db.PedidoDtlDAO().getAllDetailsByHeaderId(pedidoId)
                withContext(Dispatchers.Main) {
                    pedidoHdrLiveData.value = hdr
                    pedidoDtlLiveData.value = details
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var currentLocalId: Int? = null
}

