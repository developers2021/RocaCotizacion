package com.example.rocacotizacion.ui.PrintClass

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object HtmlTemplates {
    fun getHtmlForPdf(
        codigopedido: String, fechaEmision: String, tipoventa: String, clientenombre: String,
        codigocliente: String, rtncliente: String, rutanombre: String, vendedornombre: String,
        tableRows: String, subtotal: Double, totaldescuento: Double, total: Double, numeroletras: String, impuesto: Double
    ): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }
        val df = DecimalFormat("#,##0.00", symbols)
        val tipoVentaDisplay = when (tipoventa) {
            "CTADO" -> "CONTADO"
            "CRED" -> "CREDITO"
            else -> tipoventa
        }
        return """
        <html>
        <head>
            <title>PDF Document</title>
            <style>
                body {
                    font-family: 'Arial', sans-serif;
                    margin: 0;
                    padding: 0;
                    font-size: 30px;
                    width: 88mm;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                }
                th,
                td {
                    padding: 5px;
                    text-align: left;
                    font-size: 28px;
                    word-wrap: break-word;
                }
                th {
                    border-bottom: 1px solid black;
                    text-align: center;
                }
                .centered {
                    text-align: center;
                }
                .left-aligned {
                    text-align: left;
                }
                .right-aligned {
                    text-align: right;
                }
            </style>
        </head>

        <body>
            <div class="page-content">
                <p class="centered">Comercial La Roca, S. de R.L.</p>
                <p class="centered">SPS, 1 A., 5 C. Bo. Guamilito</p>
                <p class="centered">contabilidadlaroca@gmail.com</p>
                <p class="centered">05019002058978</p>
                <p class="centered">2516-4076 / 2516-4189</p>
                <p class="left-aligned">Pedido: ${codigopedido}</p>
                <p class="left-aligned">Fecha Emisión: ${fechaEmision}</p>
                
                <p class="left-aligned">Tipo de Venta: ${tipoVentaDisplay}</p> 
                
                <p class="left-aligned">Cliente: ${clientenombre}</p>
                <p class="left-aligned">Código Cliente: ${codigocliente}</p>
                <p class="left-aligned">RTN Cliente: ${rtncliente}</p>
                <p class="left-aligned">Ruta: ${rutanombre}</p>
                <p class="left-aligned">Vendedor: ${vendedornombre}</p>

                <table>
                    <thead>
                        <tr>
                            <th style="width: 10%;">Und</th>
                            <th style="width: 50%;">Prod</th>
                            <th style="width: 20%;">Precio</th>
                            <th style="width: 20%;">Monto</th>
                            <th style="width: 5%;"></th> <!-- Columna para el asterisco -->
                        </tr>
                    </thead>
                    <tbody>
                        ${tableRows}
                    </tbody>
                </table>

                <p class="right-aligned">Subtotal: ${df.format(subtotal)}</p>
                <p class="right-aligned">Total Descuento: ${df.format(totaldescuento)}</p>
                <p class="right-aligned">Total Impuesto: ${df.format(impuesto)}</p>
                <p class="right-aligned" style="font-weight: bold;">Total: ${df.format(total)}</p>
                <br/>
                <p class="centered">*${numeroletras}*</p>
                <p class="centered">Original</p>
            </div>
        </body>
        </html>
        """
    }

    fun getHtmlForCierreDia(
        fechaEmision: String,
        rutanombre: String,
        vendedornombre: String,
        total: Double,
        contado: Double,
        credito: Double
    ): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }
        val df = DecimalFormat("#,##0.00", symbols)

        return """
        <html>
        <head>
            <title>Cierre del Día</title>
           <style>
                body {
                    font-family: 'Arial', sans-serif;
                    margin: 0;
                    padding: 0;
                    font-size: 30px;
                    width: 88mm;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                }
                th,
                td {
                    padding: 5px;
                    text-align: left;
                    font-size: 28px;
                    word-wrap: break-word;
                }
                th {
                    border-bottom: 1px solid black;
                    text-align: center;
                }
                .centered {
                    text-align: center;
                }
                .left-aligned {
                    text-align: left;
                }
                .right-aligned {
                    text-align: right;
                }
            </style>
        </head>
        <body>
            <h2 class="center">Comercial La Roca S. de R.L.</h2>
            <p>Fecha: ${fechaEmision}</p>
            <p>Ruta: ${rutanombre}</p>
            <p>Vendedor: ${vendedornombre}</p>
            <hr />
            <table>
                <tr>
                    <td>Contado:</td>
                    <td class="right-aligned">L ${df.format(contado)}</td>
                </tr>
                <tr>
                    <td>Crédito:</td>
                    <td class="right-aligned">L ${df.format(credito)}</td>
                </tr>
                <tr>
                    <td colspan="2">
                        <hr />
                    </td>
                </tr>
                <tr>
                    <td>Total:</td>
                    <td class="right-aligned bold">L ${df.format(total)}</td>
                </tr>
            </table>
        </body>
        </html>
    """
    }









}

