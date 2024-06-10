package com.example.rocacotizacion.ui.PrintClass

data class ProductDetail(val und: String, val prod: String, val precio: Double, val monto: Double)

fun generateTableRows(details: List<ProductDetail>): String {
    return details.joinToString(separator = "") { detail ->
        """
        <tr>
            <td>${detail.und}</td>
            <td>${detail.prod}</td>
            <td>${"%.2f".format(detail.precio)}</td>
            <td>${"%.2f".format(detail.monto)}</td>
        </tr>
        """
    }
}
