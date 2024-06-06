package com.example.rocacotizacion

class Utilidades {
    companion object {
        //pruebas
        const val URL_WEB_SERVICE = "https://2d8d-2803-7780-6-5380-a4ca-3553-4a66-ce78.ngrok-free.app"
        //produccion
       // const val URL_WEB_SERVICE = "https://preventaapimovil.azurewebsites.net"
        const val URL_AUTH_LOGIN = "$URL_WEB_SERVICE/UserAuth"
        const val URL_SAVE_JSON_PEDIDO = "$URL_WEB_SERVICE/SaveJsonPedido"

    }
}
