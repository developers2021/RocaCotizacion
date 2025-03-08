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
        val mainRow = """
        <tr>
            <td>${detail.und}</td>
            <td>${detail.prod}</td>
            <td style="text-align: right;">${"%.2f".format(detail.precio)}</td>
            <td style="text-align: right;">${"%.2f".format(detail.monto)}</td>
            <td style="text-align: right; font-size: 40px;">${if (detail.impuesto < 0.1) "*" else ""}</td>
        </tr>
        """
        if (detail.descuento > 0) {
            mainRow + """
            <tr>
                <td></td>
                <td></td>
                <td style="text-align: right; font-size: 24px; font-style: italic;">Descuento:</td>
                <td style="text-align: right; font-size: 24px; ">${"%.2f".format(detail.descuento)}</td>
                <td></td>
            </tr>
            """
        } else {
            mainRow
        }
    }
}



