package com.example.rocacotizacion.ui.PrintClass

data class ProductDetail(
    val und: String,
    val prod: String,
    val precio: Double,
    val monto: Double,
    val impuesto:Double,
    val descuento:Double
)

fun generateTableRows(details: List<ProductDetail>): String {
    return details.joinToString(separator = "") { detail ->
        """
        <tr>
            <td>${detail.und}</td>
            <td>${detail.prod}</td>
            <td style="text-align: right;">${"%.2f".format(detail.precio)}</td> <!-- Precio alineado a la derecha -->
            <td style="text-align: right;">${"%.2f".format(detail.monto)}</td> <!-- Monto alineado a la derecha -->
            <td style="text-align: right; font-size: 40px;">${if (detail.impuesto < 0.1) "*" else ""}</td> <!-- Asterisco alineado a la derecha y más grande -->
        </tr>
        """
    }
}

