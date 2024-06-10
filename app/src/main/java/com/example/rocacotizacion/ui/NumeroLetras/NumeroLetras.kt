package com.example.rocacotizacion.ui.NumeroLetras

import java.math.BigInteger
import java.util.Locale
import java.util.regex.Pattern


object NumeroLetras {
    private val UNIDADES = arrayOf(
        "",
        "un ",
        "dos ",
        "tres ",
        "cuatro ",
        "cinco ",
        "seis ",
        "siete ",
        "ocho ",
        "nueve "
    )
    private val DECENAS = arrayOf(
        "diez ", "once ", "doce ", "trece ", "catorce ", "quince ", "dieciseis ",
        "diecisiete ", "dieciocho ", "diecinueve", "veinte ", "treinta ", "cuarenta ",
        "cincuenta ", "sesenta ", "setenta ", "ochenta ", "noventa "
    )
    private val CENTENAS = arrayOf(
        "",
        "ciento ",
        "doscientos ",
        "trecientos ",
        "cuatrocientos ",
        "quinientos ",
        "seiscientos ",
        "setecientos ",
        "ochocientos ",
        "novecientos "
    )

    fun Convertir(
        numero: String,
        etiquetaEnteroSingular: String,
        etiquetaEnteroPlural: String,
        etiquetaFlotanteSingular: String,
        etiquetaFlotantePlural: String,
        etiquetaConector: String,
        mayusculas: Boolean
    ): String {
        var numero = numero
        var literal = ""
        var parte_decimal = ""
        //si el numero utiliza (.) en lugar de (,) -> se reemplaza
        numero = numero.replace(".", ",")
        //si el numero no tiene parte decimal, se le agrega ,00
        if (numero.indexOf(",") == -1) {
            numero = "$numero,00"
        }
        //se valida formato de entrada -> 0,00 y 999 999 999 999,00
        if (Pattern.matches("\\d{1,12},\\d{1,2}", numero)) {
            //se divide el numero 0000000,00 -> entero y decimal
            val Num = numero.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            //de da formato al numero decimal
            if (Num[1].length == 1) {
                Num[1] += "0"
            }
            val d = getDecenas(Num[1])
            if (d !== "") {
                if (etiquetaEnteroSingular !== "") parte_decimal += " "
                parte_decimal += if (Num[1].toInt() == 1) {
                    "$etiquetaConector $d$etiquetaFlotanteSingular"
                } else {
                    "$etiquetaConector $d$etiquetaFlotantePlural"
                }
            }


            //se convierte el numero a literal
            val parteEntera = BigInteger(Num[0])

            literal = if (parteEntera.compareTo(BigInteger("0")) == 0) { //si el valor es cero
                "cero "
            } else if (parteEntera.compareTo(BigInteger("999999999")) == 1) { //si es billon
                getBillones(Num[0])
            } else if (parteEntera.compareTo(BigInteger("999999")) == 1) { //si es millon
                getMillones(Num[0])
            } else if (parteEntera.compareTo(BigInteger("999")) == 1) { //si es miles
                getMiles(Num[0])
            } else if (parteEntera.compareTo(BigInteger("99")) == 1) { //si es centena
                getCentenas(Num[0])
            } else if (parteEntera.compareTo(BigInteger("9")) == 1) { //si es decena
                getDecenas(Num[0])
            } else { //sino unidades -> 9
                getUnidades(Num[0])
            }

            //devuelve el resultado en mayusculas o minusculas
            literal += if (parteEntera.compareTo(BigInteger("1")) == 0) {
                etiquetaEnteroSingular
            } else {
                etiquetaEnteroPlural
            }

            return if (mayusculas) {
                (literal + parte_decimal).uppercase(Locale.getDefault())
            } else {
                literal + parte_decimal
            }
        } else { //error, no se puede convertir
            return null.also { literal = it!! }!!
        }
    }

    /* funciones para convertir los numeros a literales */
    private fun getUnidades(numero: String): String { // 1 - 9
        //si tuviera algun 0 antes se lo quita -> 09 = 9 o 009=9
        val num = numero.substring(numero.length - 1)
        return UNIDADES[num.toInt()]
    }

    private fun getDecenas(num: String): String { // 99
        val n = num.toInt()
        if (n < 10) { //para casos como -> 01 - 09
            return getUnidades(num)
        } else if (n > 19) { //para 20...99
            val u = getUnidades(num)
            if (u == "") { //para 20,30,40,50,60,70,80,90
                return DECENAS[num.substring(0, 1).toInt() + 8]
            } else {
                if (n == 21) {
                    return DECENAS[num.substring(0, 1).toInt() + 8].substring(0, 5) + "i" + u
                }
                if (n == 22) {
                    return DECENAS[num.substring(0, 1).toInt() + 8].substring(0, 5) + "i" + u
                }
                if (n == 23) {
                    return DECENAS[num.substring(0, 1).toInt() + 8].substring(0, 5) + "i" + u
                }
                if (n == 24) {
                    return DECENAS[num.substring(0, 1).toInt() + 8].substring(0, 5) + "i" + u
                }
                if (n == 25) {
                    return DECENAS[num.substring(0, 1).toInt() + 8].substring(0, 5) + "i" + u
                }
                if (n == 26) {
                    return DECENAS[num.substring(0, 1).toInt() + 8].substring(0, 5) + "i" + u
                }
                if (n == 27) {
                    return DECENAS[num.substring(0, 1).toInt() + 8].substring(0, 5) + "i" + u
                }
                if (n == 28) {
                    return DECENAS[num.substring(0, 1).toInt() + 8].substring(0, 5) + "i" + u
                }
                if (n == 29) {
                    return DECENAS[num.substring(0, 1).toInt() + 8].substring(0, 5) + "i" + u
                }
                return DECENAS[num.substring(0, 1).toInt() + 8] + "y " + u
            }
        } else { //numeros entre 11 y 19
            return DECENAS[n - 10]
        }
    }

    private fun getCentenas(num: String): String { // 999 o 099
        return if (num.toInt() > 99) { //es centena
            if (num.toInt() == 100) { //caso especial
                " cien "
            } else {
                CENTENAS[num.substring(0, 1).toInt()] + getDecenas(num.substring(1))
            }
        } else { //por Ej. 099
            //se quita el 0 antes de convertir a decenas
            getDecenas(num.toInt().toString() + "")
        }
    }

    private fun getMiles(numero: String): String { // 999 999
        //obtiene las centenas
        val c = numero.substring(numero.length - 3)
        //obtiene los miles
        val m = numero.substring(0, numero.length - 3)
        var n = ""
        //se comprueba que miles tenga valor entero
        if (m.toInt() > 0) {
            n = getCentenas(m)
            return n + "mil " + getCentenas(c)
        } else {
            return "" + getCentenas(c)
        }
    }

    private fun getMillones(numero: String): String { //000 000 000
        //se obtiene los miles
        val miles = numero.substring(numero.length - 6)
        //se obtiene los millones
        val millon = numero.substring(0, numero.length - 6)
        var n = ""
        if (millon.toInt() > 0) {
            n = if (millon.toInt() == 1) {
                getUnidades(millon) + "millon "
            } else {
                getCentenas(millon) + "millones "
            }
        }

        return n + getMiles(miles)
    }

    private fun getBillones(numero: String): String { //000 000 000 000
        //se obtiene los miles
        val miles = numero.substring(numero.length - 9)
        //se obtiene los millones
        val millon = numero.substring(0, numero.length - 9)
        var n = ""
        n = if (millon.toInt() == 1) {
            getUnidades(millon) + "billon "
        } else {
            getCentenas(millon) + "billones "
        }

        return n + getMillones(miles)
    }
}


