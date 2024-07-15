package com.example.rocacotizacion

class Utilidades {
    companion object {
        //pruebas
        const val URL_WEB_SERVICE = "https://6167-2803-7780-6-5380-b522-7c08-72fa-7e44.ngrok-free.app"
        //produccion
       // const val URL_WEB_SERVICE = "https://preventaapimovil.azurewebsites.net"
        const val URL_AUTH_LOGIN = "$URL_WEB_SERVICE/UserAuth"
        const val URL_SAVE_JSON_PEDIDO = "$URL_WEB_SERVICE/SaveJsonPedido"

    }
}
