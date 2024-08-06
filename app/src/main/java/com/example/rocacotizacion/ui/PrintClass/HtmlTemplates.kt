package com.example.rocacotizacion.ui.PrintClass

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HtmlTemplates {
    fun getHtmlForPdf(pedidoId: String, fechaEmision: String, tipoventa: String, clientenombre: String,
                      codigocliente: String, rtncliente: String, rutanombre: String, vendedornombre: String,
                      tableRows: String,subtotal: Double,totaldescuento:Double,total:Double,numeroletras:String,impuesto:Double): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        symbols.setGroupingSeparator(',')
        symbols.setDecimalSeparator('.')
        val df = DecimalFormat("#,##0.00", symbols)
        return """
        <html>
<head>
    <title>PDF Document</title>
    <style>
        body {
            font-family: 'Arial', sans-serif;
            margin: 0;
            padding: 0;
            font-size: 14px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th,
        td {
            padding: 8px;
            text-align: left;
        }

        th {
            border-bottom: 1px solid black;
            /* Only add bottom border to the header */
        }

        .centered {
            text-align: center;
        }

        .left-aligned {
            text-align: left;
            padding-left: 15px;
        }
         
        .right-aligned {
            text-align: right;
            padding-right: 15px;
        }
    </style>
</head>

<body>
    <div class="page-content">
        <p class="centered">Comercial La Roca, S. de R.L.</p>
        <p class="centered">SPS, 1 A., 5 C. Bo. Guamilito</p>
        <p class="centered">contabilidadlaroca@gmail.com</p>
        <p class="centered">05119002058978</p>
        <p class="centered">2516-4076 / 2516-4189</p>
        <p class="left-aligned">Pedido: ${pedidoId}</p>
        <p class="left-aligned">Fecha Emision: ${fechaEmision}</p>
        <p class="left-aligned">Tipo de Venta: ${tipoventa}</p>
        <p class="left-aligned">Cliente:  ${clientenombre}</p>
        <p class="left-aligned">Codigo Cliente:  ${codigocliente}</p>
        <p class="left-aligned">RTN Cliente:  ${rtncliente}</p>
        <p class="left-aligned">Ruta:  ${rutanombre}</p>
        <p class="left-aligned">Vendedor:  ${vendedornombre}</p>
        <table>
            <thead>
                <tr>
                    <th>Und</th>
                    <th>Prod</th>
                    <th>Precio</th>
                    <th>Monto</th>
                </tr>
            </thead>
            <tbody>
                $tableRows
            </tbody>
        </table>
                <p class="right-aligned">Subtotal: ${df.format(subtotal)}</p>
                <p class="right-aligned">Total Descuento: ${df.format(totaldescuento)}</p>
                <p class="right-aligned">Total Impuesto: ${df.format(impuesto)}</p>
                <p class="right-aligned">Total: ${df.format(total)}</p>
                <br/ >
                <p class="centered">*${numeroletras}*</p>
                <p class="centered">Original</p>
    </div>
</body>

</html>
    """
    }

}
