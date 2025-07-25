package com.example.rocacotizacion

class Utilidades {

    companion object {
        //pruebas
        const val URL_WEB_SERVICE = "https://glider-logical-explicitly.ngrok-free.app"
        //produccion
        //const val URL_WEB_SERVICE = "https://preventaapimovil.azurewebsites.net"
        const val URL_AUTH_LOGIN = "$URL_WEB_SERVICE/UserAuth"
        const val URL_SAVE_JSON_PEDIDO = "$URL_WEB_SERVICE/SaveJsonPedido"

        // Endpoints para sincronización parcial
        const val URL_CLIENTES = "$URL_WEB_SERVICE/Clientes"
        const val URL_PRODUCTOS = "$URL_WEB_SERVICE/Productos"
        const val URL_PRECIOS_GRUPOS = "$URL_WEB_SERVICE/PreciosGrupos"
        const val URL_DESCUENTOS = "$URL_WEB_SERVICE/Descuentos"
        
    }
}

