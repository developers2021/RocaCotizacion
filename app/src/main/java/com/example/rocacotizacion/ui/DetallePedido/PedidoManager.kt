package com.tuapp.nombredepaquete

import android.content.Context
import android.content.SharedPreferences

// Clase Singleton para manejar los códigos de pedido
object PedidoManager {
    private var currentLocalId: Int? = null
    private lateinit var prefs: SharedPreferences

    // Inicializa PedidoManager con el último ID de pedido y el contexto
    fun initialize(ultimoIdPedido: Int, context: Context) {
        prefs = context.getSharedPreferences("pedido_prefs", Context.MODE_PRIVATE)
        if (currentLocalId == null) {
            currentLocalId = prefs.getInt("lastLocalId", ultimoIdPedido + 1)
        }
    }

    // Genera el siguiente código de pedido
    fun getNextCodigoPedido(codigoPuntoEmision: String): String {
        if (currentLocalId == null) {
            throw IllegalStateException("PedidoManager no está inicializado")
        }

        val codigoPedido = "$codigoPuntoEmision${String.format("%08d", currentLocalId)}"
        currentLocalId = currentLocalId!! + 1

        // Guarda el último ID actualizado en SharedPreferences
        prefs.edit().putInt("lastLocalId", currentLocalId!!).apply()

        return codigoPedido
    }

    fun clearData() {
        currentLocalId = null
        prefs.edit().remove("lastLocalId").apply()
    }


}
