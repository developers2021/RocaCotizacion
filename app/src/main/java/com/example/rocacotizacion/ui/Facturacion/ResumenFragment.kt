package com.example.rocacotizacion.ui.Facturacion

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.DAO.PedidoDtl
import com.example.rocacotizacion.DAO.PedidoHdr
import com.example.rocacotizacion.DTO.SharedDataModel
import com.example.rocacotizacion.DataModel.PedidoPrintModel
import com.example.rocacotizacion.DataModel.PedidoViewModel
import com.example.rocacotizacion.R
import com.example.rocacotizacion.ui.PrintClass.HtmlTemplates
import com.example.rocacotizacion.ui.PrintClass.generateTableRows
import com.example.rocacotizacion.ui.PrintUtility.NumeroLetras
import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.tool.xml.XMLWorkerHelper
import com.tuapp.nombredepaquete.PedidoManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.math.roundToLong


class ResumenFragment : Fragment() {

        private lateinit var resumenAdapter: ResumenAdapter
        private var isEscalaDiscountEnabled: Boolean = false
        private lateinit var backPressedCallback: OnBackPressedCallback
        private lateinit var btnCancelPedido: ImageButton
        private var isPedidoGuardado = false
        private val pedidoViewModel: PedidoViewModel by viewModels()

    // Add these properties to track switch states
    private var isDescuentoRutaActivado = false
    private var isDescuentoEscalaActivado = false
    private var isDescuentoTipoPagoActivado = false
    private var oldItemCount = 0
    private var oldSubtotal = 0.0


    // Agregamos referencias a los botones como variables de clase
        private lateinit var btnsavepedido: Button
        private lateinit var btnExit: Button

        fun setStatusBarColor(context: Context) {
            val window = (context as Activity).window

            // Cambia el color del Status Bar
            window.statusBarColor = ContextCompat.getColor(context, R.color.grayDark)
        }


    private fun applyEscalaDiscounts() {
        CoroutineScope(Dispatchers.IO).launch {
            SharedDataModel.detalleItems.value?.forEach { item ->
                Log.d("applyEscalaDiscounts", "-----------------------------")
                Log.d("applyEscalaDiscounts", "📌 Producto: ${item.codigoproducto}, Cantidad: ${item.quantity}")

                // Obtener descuentos de la base de datos
                val escalaDiscounts = DatabaseApplication.getDatabase(requireContext())
                    .invdescuentoporescalaDAO()
                    .getDescuentoPorEscala(item.codigoproducto)

                Log.d("applyEscalaDiscounts", "🎯 Descuentos disponibles: $escalaDiscounts")

                // Filtrar descuento según la cantidad del producto
                val escalaDiscount = escalaDiscounts.firstOrNull {
                    item.quantity >= it.rangoinicial && item.quantity <= it.rangofinal
                }

                // Asignar porcentaje de descuento
                item.porcentajeEscala = escalaDiscount?.monto ?: 0.0
                Log.d("applyEscalaDiscounts", "✅ Descuento aplicado: ${item.porcentajeEscala}%")

                // Calcular porcentaje total de descuentos
                item.porcentajeTotal = item.porcentajeEscala + item.porcentajeTipoPago + item.porcentajeRuta
                Log.d("applyEscalaDiscounts", "📊 Porcentaje total de descuento: ${item.porcentajeTotal}%")

                // Convertir a BigDecimal para evitar errores de precisión
                val price = BigDecimal.valueOf(item.price)
                val quantity = BigDecimal.valueOf(item.quantity.toDouble())
                val porcentajeTotal = BigDecimal.valueOf(item.porcentajeTotal)
                val porcentajeImpuesto = BigDecimal.valueOf(item.porcentajeImpuesto)

                // 1) Subtotal exacto y truncado
                val rawSubtotal   = price.multiply(quantity)
                val subtotal      = rawSubtotal.setScale(2, RoundingMode.DOWN)
                Log.d("applyEscalaDiscounts", "💰 Subtotal: $subtotal")

                // 2) Descuento (porcentaje) calculado y truncado
                val rawDescuento  = rawSubtotal.multiply(porcentajeTotal)
                    .divide(BigDecimal(100), 10, RoundingMode.HALF_UP)
                val descuento     = rawDescuento.setScale(2, RoundingMode.DOWN)
                Log.d("applyEscalaDiscounts", "💸 Descuento: $descuento")

                // 3) Base imponible
                val baseImponible = rawSubtotal.subtract(descuento)
                Log.d("applyEscalaDiscounts", "⚖️ Base Imponible: $baseImponible")

                // 4) Impuesto siempre hacia arriba
                val rawImpuesto   = baseImponible.multiply(porcentajeImpuesto)
                    .divide(BigDecimal(100), 10, RoundingMode.HALF_UP)
                val valorImpuesto = rawImpuesto.setScale(2, RoundingMode.HALF_UP)
                Log.d("applyEscalaDiscounts", "🧾 Impuesto: $valorImpuesto")

                // 5) Total neto + impuesto truncado
                val total         = baseImponible.add(valorImpuesto)
                    .setScale(2, RoundingMode.HALF_UP)
                Log.d("applyEscalaDiscounts", "🏷️ Total: $total")

                // Asignar valores al item
                item.subtotal        = subtotal.toDouble()
                item.descuento       = descuento.toDouble()
                item.valorimpuesto   = valorImpuesto.toDouble()
                item.total           = total.toDouble()
                item.checkedDescuentoEscala = true

                Log.d("applyEscalaDiscounts", "✅ Valores finales: Subtotal=${item.subtotal}, Descuento=${item.descuento}, Impuesto=${item.valorimpuesto}, Total=${item.total}")
                Log.d("applyEscalaDiscounts", "-----------------------------")
            }

            // Publicar cambios
            SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)

            withContext(Dispatchers.Main) {
                updateTotals()
            }
        }
    }

    private fun applyTipoVentaDiscounts() {
        CoroutineScope(Dispatchers.IO).launch {
            val tipopago = activity?.intent?.getStringExtra("tipoPago")

            SharedDataModel.detalleItems.value?.forEach { item ->
                Log.d("applyTipoVentaDiscounts", "-----------------------------")
                Log.d("applyTipoVentaDiscounts", "📌 Producto: ${item.codigoproducto}, Cantidad: ${item.quantity}")

                // Obtener descuento de la base de datos basado en el tipo de pago
                val discountData = tipopago?.let {
                    DatabaseApplication.getDatabase(requireContext())
                        .invdescuentoportipoventaDAO()
                        .getDescuentoPorTipoVenta(item.codigoproducto, it)
                }

                Log.d("applyTipoVentaDiscounts", "🎯 Descuento por Tipo de Pago: ${discountData?.monto ?: 0.0}")

                // Asignar el porcentaje de descuento
                item.porcentajeTipoPago = discountData?.monto ?: 0.0
                item.porcentajeTotal = item.porcentajeEscala + item.porcentajeTipoPago + item.porcentajeRuta
                Log.d("applyTipoVentaDiscounts", "📊 Porcentaje total de descuento: ${item.porcentajeTotal}%")

                // Convertir valores a BigDecimal
                val price            = BigDecimal.valueOf(item.price)
                val quantity         = BigDecimal.valueOf(item.quantity.toDouble())
                val porcentajeTotal  = BigDecimal.valueOf(item.porcentajeTotal)
                val porcentajeImpuesto = BigDecimal.valueOf(item.porcentajeImpuesto)

                // 1) Subtotal exacto y truncado
                val rawSubtotal   = price.multiply(quantity)
                val subtotal      = rawSubtotal.setScale(2, RoundingMode.DOWN)
                Log.d("applyTipoVentaDiscounts", "💰 Subtotal: $subtotal")

                // 2) Descuento (porcentaje) calculado y truncado
                val rawDescuento  = rawSubtotal.multiply(porcentajeTotal)
                    .divide(BigDecimal(100), 10, RoundingMode.HALF_UP)
                val descuento     = rawDescuento.setScale(2, RoundingMode.DOWN)
                Log.d("applyTipoVentaDiscounts", "💸 Descuento: $descuento")

                // 3) Base imponible
                val baseImponible = rawSubtotal.subtract(descuento)
                Log.d("applyTipoVentaDiscounts", "⚖️ Base Imponible: $baseImponible")

                // 4) Impuesto siempre hacia arriba
                val rawImpuesto   = baseImponible.multiply(porcentajeImpuesto)
                    .divide(BigDecimal(100), 10, RoundingMode.HALF_UP)
                val valorImpuesto = rawImpuesto.setScale(2, RoundingMode.HALF_UP)
                Log.d("applyTipoVentaDiscounts", "🧾 Impuesto: $valorImpuesto")

                // 5) Total neto + impuesto truncado
                val total         = baseImponible.add(valorImpuesto)
                    .setScale(2, RoundingMode.HALF_UP)
                Log.d("applyTipoVentaDiscounts", "🏷️ Total: $total")

                // Asignar valores al item
                item.subtotal         = subtotal.toDouble()
                item.descuento        = descuento.toDouble()
                item.valorimpuesto    = valorImpuesto.toDouble()
                item.total            = total.toDouble()
                item.checkedDescuentoTipoPago = true

                Log.d("applyTipoVentaDiscounts", "✅ Valores finales: Subtotal=${item.subtotal}, Descuento=${item.descuento}, Impuesto=${item.valorimpuesto}, Total=${item.total}")
                Log.d("applyTipoVentaDiscounts", "-----------------------------")
            }

            // Publicar cambios
            SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)

            withContext(Dispatchers.Main) {
                updateTotals()
            }
        }
    }

    private fun applyRutaDiscounts() {
        CoroutineScope(Dispatchers.IO).launch {
            val agente = DatabaseApplication.getDatabase(requireContext()).AgenteDAO().getAgente()
            val idruta = agente.idruta

            SharedDataModel.detalleItems.value?.forEach { item ->
                Log.d("applyRutaDiscounts", "-----------------------------")
                Log.d("applyRutaDiscounts", "📌 Producto: ${item.codigoproducto}, Cantidad: ${item.quantity}, ID Ruta: $idruta")

                // Obtener descuento de la base de datos basado en la ruta y producto
                val discountData = DatabaseApplication.getDatabase(requireContext())
                    .invdescuentoporrutaDAO()
                    .getDescuentoPorRuta(idruta, item.codigoproducto)

                Log.d("applyRutaDiscounts", "🎯 Descuento por Ruta: ${discountData?.monto ?: 0.0}")

                // Asignar el porcentaje de descuento por ruta
                item.porcentajeRuta = discountData?.monto ?: 0.0
                item.porcentajeTotal = item.porcentajeEscala + item.porcentajeRuta + item.porcentajeTipoPago
                Log.d("applyRutaDiscounts", "📊 Porcentaje total de descuento: ${item.porcentajeTotal}%")

                // Convertir valores a BigDecimal
                val price            = BigDecimal.valueOf(item.price)
                val quantity         = BigDecimal.valueOf(item.quantity.toDouble())
                val porcentajeTotal  = BigDecimal.valueOf(item.porcentajeTotal)
                val porcentajeImpuesto = BigDecimal.valueOf(item.porcentajeImpuesto)

                // 1) Subtotal exacto y truncado
                val rawSubtotal   = price.multiply(quantity)
                val subtotal      = rawSubtotal.setScale(2, RoundingMode.DOWN)
                Log.d("applyRutaDiscounts", "💰 Subtotal: $subtotal")

                // 2) Descuento (porcentaje) calculado y truncado
                val rawDescuento  = rawSubtotal.multiply(porcentajeTotal)
                    .divide(BigDecimal(100), 10, RoundingMode.HALF_UP)
                val descuento     = rawDescuento.setScale(2, RoundingMode.DOWN)
                Log.d("applyRutaDiscounts", "💸 Descuento: $descuento")

                // 3) Base imponible
                val baseImponible = rawSubtotal.subtract(descuento)
                Log.d("applyRutaDiscounts", "⚖️ Base Imponible: $baseImponible")

                // 4) Impuesto siempre hacia arriba
                val rawImpuesto   = baseImponible.multiply(porcentajeImpuesto)
                    .divide(BigDecimal(100), 10, RoundingMode.HALF_UP)
                val valorImpuesto = rawImpuesto.setScale(2, RoundingMode.HALF_UP)
                Log.d("applyRutaDiscounts", "🧾 Impuesto: $valorImpuesto")

                // 5) Total neto + impuesto truncado
                val total         = baseImponible.add(valorImpuesto)
                    .setScale(2, RoundingMode.HALF_UP)
                Log.d("applyRutaDiscounts", "🏷️ Total: $total")

                // Asignar valores al item
                item.subtotal       = subtotal.toDouble()
                item.descuento      = descuento.toDouble()
                item.valorimpuesto  = valorImpuesto.toDouble()
                item.total          = total.toDouble()
                item.checkedDescuentoRuta = true

                Log.d("applyRutaDiscounts", "✅ Valores finales: Subtotal=${item.subtotal}, Descuento=${item.descuento}, Impuesto=${item.valorimpuesto}, Total=${item.total}")
                Log.d("applyRutaDiscounts", "-----------------------------")
            }

            // Publicar cambios
            SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)

            withContext(Dispatchers.Main) {
                updateTotals()
            }
        }
    }






    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setStatusBarColor(requireContext())

        // Inicializamos los switches
        val switchEscala = view.findViewById<SwitchCompat>(R.id.switchOption1)
        val switchTipoPago = view.findViewById<SwitchCompat>(R.id.switchOption2)
        val switchRuta = view.findViewById<SwitchCompat>(R.id.switchOption3)

        configureSwitchColors(switchEscala, requireContext())
        configureSwitchColors(switchTipoPago, requireContext())
        configureSwitchColors(switchRuta, requireContext())

        // Inicializamos los botones
        val btnCancelPedido: ImageButton = view.findViewById(R.id.btnCancelPedido)
        val modoboton = activity?.intent?.getStringExtra("modo") ?: "crear"
        if (modoboton == "editar") {
            // Cambiamos el ícono, por ejemplo, a un "cerrar" (ic_close)
            btnCancelPedido.setImageResource(R.drawable.ic_close)
        } else {
            // Modo crear → usas el ícono de basura si quieres
            btnCancelPedido.setImageResource(R.drawable.ic_trash)
        }
        btnsavepedido = view.findViewById(R.id.btnsavepedido)

        // Indicador de si el pedido ha sido guardado
        var isPedidoGuardado = false

        // Inicializamos el callback para bloquear el botón de atrás
        val backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isPedidoGuardado) {
                    // Permitir salir si el pedido ya se guardó
                    requireActivity().finish()
                } else {
                    Toast.makeText(context, "No puedes salir sin cancelar el pedido.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)


        // ===== LÓGICA PARA MODO EDICIÓN: LEER ENCABEZADO Y CONFIGURAR SWITCHES =====
        val modo = activity?.intent?.getStringExtra("modo") ?: "crear"
        val pedidoId = activity?.intent?.getIntExtra("pedidoId", -1) ?: -1

        if (modo == "editar" && pedidoId != -1) {
            // Cargar el encabezado desde la BD para obtener descuentoEscalaActivado, etc.
            CoroutineScope(Dispatchers.IO).launch {
                val db = DatabaseApplication.getDatabase(requireContext())
                val existingHdr = db.PedidoHdrDAO().getPedidoPrinteById(pedidoId)

                withContext(Dispatchers.Main) {
                    // Ajustar el estado de los switches según los campos guardados
                    switchEscala.isChecked = existingHdr.descuentoEscalaActivado
                    switchTipoPago.isChecked = existingHdr.descuentoTipoPagoActivado
                    switchRuta.isChecked = existingHdr.descuentoRutaActivado

                    // También puedes ajustar tus variables booleanas (si las usas):

                }
            }
        }




        // SWITCH DESCUENTO POR ESCALA
        switchEscala.setOnCheckedChangeListener { _, isChecked ->
            isDescuentoEscalaActivado = isChecked
            isEscalaDiscountEnabled = isChecked
            if (isChecked) {
                applyEscalaDiscounts()
            } else {
                removeEscalaDiscounts()
                updateTotals()
            }
        }

        // SWITCH DESCUENTO POR TIPO DE PAGO
        switchTipoPago.setOnCheckedChangeListener { _, isChecked ->
            isDescuentoTipoPagoActivado = isChecked
            if (isChecked) {
                applyTipoVentaDiscounts()
            } else {
                SharedDataModel.detalleItems.value?.forEach {
                    it.porcentajeTipoPago = 0.0
                    it.porcentajeTotal = it.porcentajeEscala + it.porcentajeRuta
                    it.descuento = (it.price * it.quantity) * (it.porcentajeTotal / 100)
                    it.subtotal = (it.price * it.quantity)
                    it.valorimpuesto = (it.subtotal - it.descuento) * (it.porcentajeImpuesto / 100)
                    it.total = (it.subtotal + it.valorimpuesto) - it.descuento
                    it.checkedDescuentoTipoPago = false
                }
                SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)
                updateTotals()
            }
        }

        // SWITCH DESCUENTO POR RUTA
        switchRuta.setOnCheckedChangeListener { _, isChecked ->
            isDescuentoRutaActivado = isChecked
            if (isChecked) {
                applyRutaDiscounts()
            } else {
                Log.d("SwitchRuta", "Descuento desactivado, procesando los detalles...")

                SharedDataModel.detalleItems.value?.forEach {
                    Log.d("SwitchRuta", "Antes de modificar: $it")
                    it.porcentajeRuta = 0.0
                    it.porcentajeTotal = it.porcentajeEscala + it.porcentajeTipoPago
                    it.descuento = (it.price * it.quantity) * (it.porcentajeTotal / 100)
                    it.subtotal = (it.price * it.quantity)
                    it.valorimpuesto = (it.subtotal - it.descuento) * (it.porcentajeImpuesto / 100)
                    it.total = (it.subtotal + it.valorimpuesto) - it.descuento
                    it.checkedDescuentoRuta = false
                    Log.d("SwitchRuta", "Después de modificar: $it")
                }
                Log.d("SwitchRuta", "Todos los detalles procesados, actualizando SharedDataModel...")
                SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)
                Log.d("SwitchRuta", "Actualizando totales...")
                updateTotals()
            }
        }

        // Configuración del botón "Guardar Pedido"
        btnsavepedido.setOnClickListener {
            if (SharedDataModel.detalleItems.value.isNullOrEmpty()) {
                Toast.makeText(context, "No hay items en el pedido", Toast.LENGTH_SHORT).show()
            } else {
                saveOrder()
                isPedidoGuardado = true
                btnCancelPedido.isEnabled = false // Desactivar botón de cancelar
                backPressedCallback.isEnabled = false // Permitir salir después de guardar
                btnsavepedido.text = "Salir" // Cambiar texto del botón
                btnsavepedido.setOnClickListener {
                    requireActivity().finish()
                }
            }
        }

        // Listener para el botón de cancelar pedido
        btnCancelPedido.setOnClickListener {
            cancelarPedido()
        }

        // Observamos los cambios en detalleItems
        SharedDataModel.detalleItems.observe(viewLifecycleOwner) { items ->

            // ---- LÓGICA DE HABILITAR O DESHABILITAR SWITCHES SEGÚN HAYA O NO PRODUCTOS ----
            val totalPedido = items.sumOf { it.total }
            if (items.isEmpty() || totalPedido == 0.0) {
                switchEscala.isEnabled = false
                switchEscala.isChecked = false

                switchTipoPago.isEnabled = false
                switchTipoPago.isChecked = false

                switchRuta.isEnabled = false
                switchRuta.isChecked = false
            } else {
                switchEscala.isEnabled = true
                switchTipoPago.isEnabled = true
                switchRuta.isEnabled = true
            }

            // ---- ACTUALIZA TOTALES NORMALMENTE ----
            updateTotals()

            // ---- Verifica si cambió el subtotal (en lugar de cuántos ítems hay) ----
            val currentSubtotal = items.sumOf { it.subtotal }
            if (currentSubtotal != oldSubtotal) {

                // -------------------------------------------------------
                // Si cambió el subtotal, reiniciamos DESCUENTOS ACTIVOS
                // -------------------------------------------------------

                // 1) DESCUENTO POR ESCALA
                if (isDescuentoEscalaActivado && switchEscala.isChecked) {
                    removeEscalaDiscounts()   // Ponemos a cero el descuento
                    applyEscalaDiscounts()    // Lo reaplicamos
                }

                // 2) DESCUENTO POR TIPO DE PAGO
                if (isDescuentoTipoPagoActivado && switchTipoPago.isChecked) {
                    // Primero ponemos a cero el tipo de pago actual
                    SharedDataModel.detalleItems.value?.forEach { item ->
                        item.porcentajeTipoPago = 0.0
                        item.porcentajeTotal = item.porcentajeEscala + item.porcentajeRuta
                        item.descuento = (item.price * item.quantity) * (item.porcentajeTotal / 100)
                        item.subtotal = (item.price * item.quantity)
                        item.valorimpuesto = (item.subtotal - item.descuento) * (item.porcentajeImpuesto / 100)
                        item.total = (item.subtotal + item.valorimpuesto) - item.descuento
                    }
                    SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)
                    // Luego re-aplicamos
                    applyTipoVentaDiscounts()
                }

                // 3) DESCUENTO POR RUTA
                if (isDescuentoRutaActivado && switchRuta.isChecked) {
                    // Primero ponemos a cero el descuento por ruta
                    SharedDataModel.detalleItems.value?.forEach { item ->
                        item.porcentajeRuta = 0.0
                        item.porcentajeTotal = item.porcentajeEscala + item.porcentajeTipoPago
                        item.descuento = (item.price * item.quantity) * (item.porcentajeTotal / 100)
                        item.subtotal = (item.price * item.quantity)
                        item.valorimpuesto = (item.subtotal - item.descuento) * (item.porcentajeImpuesto / 100)
                        item.total = (item.subtotal + item.valorimpuesto) - item.descuento
                    }
                    SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)
                    // Luego re-aplicamos
                    applyRutaDiscounts()
                }

                // ----------------------------------------------------------------------------
                // AQUI HACEMOS EL "REINICIO" DE LOS SWITCHES AL FINAL (Off->On)
                // ----------------------------------------------------------------------------

                // 1) Guardamos el estado actual de cada switch
                val wasEscalaChecked = switchEscala.isChecked
                val wasTipoPagoChecked = switchTipoPago.isChecked
                val wasRutaChecked = switchRuta.isChecked

                // 2) Quitamos temporalmente los listeners para que no se dispare la lógica
                switchEscala.setOnCheckedChangeListener(null)
                switchTipoPago.setOnCheckedChangeListener(null)
                switchRuta.setOnCheckedChangeListener(null)

                // 3) Los forzamos a false para "reiniciarlos"
                switchEscala.isChecked = false
                switchTipoPago.isChecked = false
                switchRuta.isChecked = false

                // 4) Regresamos cada uno a su estado anterior
                switchEscala.isChecked = wasEscalaChecked
                switchTipoPago.isChecked = wasTipoPagoChecked
                switchRuta.isChecked = wasRutaChecked

                // 5) Volvemos a colocar los listeners
                switchEscala.setOnCheckedChangeListener { _, isChecked ->
                    isDescuentoEscalaActivado = isChecked
                    isEscalaDiscountEnabled = isChecked
                    if (isChecked) {
                        applyEscalaDiscounts()
                    } else {
                        removeEscalaDiscounts()
                        updateTotals()
                    }
                }

                switchTipoPago.setOnCheckedChangeListener { _, isChecked ->
                    isDescuentoTipoPagoActivado = isChecked
                    if (isChecked) {
                        applyTipoVentaDiscounts()
                    } else {
                        SharedDataModel.detalleItems.value?.forEach {
                            it.porcentajeTipoPago = 0.0
                            it.porcentajeTotal = it.porcentajeEscala + it.porcentajeRuta
                            it.descuento = (it.price * it.quantity) * (it.porcentajeTotal / 100)
                            it.subtotal = (it.price * it.quantity)
                            it.valorimpuesto = (it.subtotal -it.descuento) * (it.porcentajeImpuesto / 100)
                            it.total = (it.subtotal + it.valorimpuesto) - it.descuento
                            it.checkedDescuentoTipoPago = false
                        }
                        SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)
                        updateTotals()
                    }
                }

                switchRuta.setOnCheckedChangeListener { _, isChecked ->
                    isDescuentoRutaActivado = isChecked
                    if (isChecked) {
                        applyRutaDiscounts()
                    } else {
                        SharedDataModel.detalleItems.value?.forEach {
                            it.porcentajeRuta = 0.0
                            it.porcentajeTotal = it.porcentajeEscala + it.porcentajeTipoPago
                            it.descuento = (it.price * it.quantity) * (it.porcentajeTotal / 100)
                            it.subtotal = (it.price * it.quantity)
                            it.valorimpuesto = (it.subtotal - it.descuento) * (it.porcentajeImpuesto / 100)
                            it.total = (it.subtotal + it.valorimpuesto) - it.descuento
                            it.checkedDescuentoRuta = false
                        }
                        SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)
                        updateTotals()
                    }
                }
            }

            // ---- Finalmente, guardamos el subtotal actual para la próxima comparación ----
            oldSubtotal = currentSubtotal
        }




        // Inicialización del RecyclerView y Adapter
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewResumen1)
        resumenAdapter = ResumenAdapter(listOf())
        recyclerView.adapter = resumenAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Mostrar el tipo de pago en el TextView correspondiente
        val textViewtipopago: TextView = view.findViewById(R.id.tipopago)
        textViewtipopago.text = activity?.intent?.getStringExtra("tipoPago")?.let { stringtipopago(it) }
    }



    private fun cancelarPedido() {
            if (isPedidoGuardado) {
                Toast.makeText(context, "El pedido ya ha sido guardado, no es necesario cancelar.", Toast.LENGTH_SHORT).show()
                return
            }

        val modo = activity?.intent?.getStringExtra("modo") ?: "crear"

        // Título y mensaje distintos según el modo
        val tituloDialogo = if (modo == "editar") {
            "Dejar de editar"
        } else {
            "Cancelar Pedido"
        }

        val mensaje = if (modo == "editar") {
            "¿Estás seguro de que deseas dejar de editar este pedido? (Se perderán los cambios.)"
        } else {
            "¿Estás seguro de que deseas cancelar el pedido?"
        }

        AlertDialog.Builder(requireContext())
            .setTitle(tituloDialogo)      // <--- Aquí se asigna dinámicamente
            .setMessage(mensaje)
            .setPositiveButton("Sí") { _, _ ->
                // Al cancelar, limpias la lista y cierras
                SharedDataModel.detalleItems.postValue(mutableListOf())
                requireActivity().finish()
            }
            .setNegativeButton("No", null)
            .setCancelable(false)
            .show()
        }





        private fun removeEscalaDiscounts() {
            SharedDataModel.detalleItems.value?.forEach {
                it.porcentajeEscala = 0.0
                it.porcentajeTotal = it.porcentajeTipoPago + it.porcentajeRuta
                it.descuento = (it.price * it.quantity) * (it.porcentajeTotal / 100)
                it.subtotal = (it.price * it.quantity)
                it.valorimpuesto = (it.subtotal - it.descuento) * (it.porcentajeImpuesto / 100)
                it.total = (it.subtotal + it.valorimpuesto) - it.descuento
                it.checkedDescuentoEscala = false
            }
            SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)
        }

        private fun updateTotals() {
            val items = SharedDataModel.detalleItems.value ?: return
            val total = items.sumOf { it.total }
            val impuesto = items.sumOf { it.valorimpuesto }
            val subtotal = items.sumOf { it.subtotal }
            val descuento = items.sumOf { it.descuento }

            val format = DecimalFormat("#,##0.00")

            view?.findViewById<TextView>(R.id.sumtotal)?.text = "L.${format.format(total)}"
            view?.findViewById<TextView>(R.id.sumsubtotal)?.text = "L.${format.format(subtotal)}"
            view?.findViewById<TextView>(R.id.sumimpuesto)?.text = "L.${format.format(impuesto)}"
            view?.findViewById<TextView>(R.id.sumdescuento)?.text = "L.${format.format(descuento)}"
        }


        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.fragment_resumen, container, false)

            // Inicializamos PedidoManager con el último ID desde la base de datos
            CoroutineScope(Dispatchers.IO).launch {
                val agente = DatabaseApplication.getDatabase(requireContext()).AgenteDAO().getAgente()
                PedidoManager.initialize(agente.ultimoidpedido ?: 0, requireContext())
            }

            val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewResumen1)
            resumenAdapter = ResumenAdapter(listOf())
            recyclerView.adapter = resumenAdapter
            recyclerView.layoutManager = LinearLayoutManager(context)

            val textViewtipopago: TextView = view.findViewById(R.id.tipopago)
            textViewtipopago.text = activity?.intent?.getStringExtra("tipoPago")
                ?.let { stringtipopago(it) }

            // Observamos los cambios en detalleItems
            SharedDataModel.detalleItems.observe(viewLifecycleOwner, Observer { items ->
                resumenAdapter.updateItems(items)
                // Actualizamos los totales
                val impuesto = String.format("%.2f", items.sumOf { it.valorimpuesto })
                val total = items.sumOf { it.total }
                val subtotal = String.format("%.2f", items.sumOf { it.subtotal })
                val roundedTotal = String.format("%.2f", total)
                val sumdescuento = String.format("%.2f", items.sumOf { it.descuento })
                view.findViewById<TextView>(R.id.sumsubtotal).text = "L.$subtotal"
                view.findViewById<TextView>(R.id.sumtotal).text = "L.$roundedTotal"
                view.findViewById<TextView>(R.id.sumimpuesto).text = "L.$impuesto"
                view.findViewById<TextView>(R.id.sumdescuento).text = "L.$sumdescuento"
            })

            // Inicializamos los botones
            btnsavepedido = view.findViewById(R.id.btnsavepedido)

            // Configuramos el listener del botón "Guardar Pedido"
            btnsavepedido.setOnClickListener {
                if (SharedDataModel.detalleItems.value.isNullOrEmpty()) {
                    Toast.makeText(context, "No hay items en el pedido", Toast.LENGTH_SHORT).show()
                } else {
                    saveOrder()
                }
            }


            btnsavepedido.setOnClickListener {
                if (SharedDataModel.detalleItems.value.isNullOrEmpty()) {
                    Toast.makeText(context, "No hay items en el pedido", Toast.LENGTH_SHORT).show()
                } else {
                    saveOrder()
                }
            }

            return view
        }

        private fun generateCodigoPedido(): String {
            val agente = DatabaseApplication.getDatabase(requireContext()).AgenteDAO().getAgente()
            val codigoPuntoEmision = agente.codigopuntoemision.toString()

            // Usar PedidoManager para generar el código
            return PedidoManager.getNextCodigoPedido(codigoPuntoEmision)
        }





        fun stringtipopago(tipopago: String): String {
            return if (tipopago == "CRED") "Credito" else "Contado"
        }



    private fun saveOrder() {
        CoroutineScope(Dispatchers.IO).launch {
            // Recupera los extras para determinar el modo y otros datos
            val modo = activity?.intent?.getStringExtra("modo") ?: "crear"
            val tipoPago = activity?.intent?.getStringExtra("tipoPago") ?: "Contado"
            val clientecodigo = activity?.intent?.getStringExtra("clientecodigo") ?: "000"
            val detalleItems = SharedDataModel.detalleItems.value ?: listOf()

            // Cálculo de totales (usando los valores ya calculados en los DetalleItem)
            val subtotal = detalleItems.sumOf { it.subtotal }
            val descuento = detalleItems.sumOf { it.descuento }
            val impuesto = detalleItems.sumOf { it.valorimpuesto }
            val total = detalleItems.sumOf { it.total }
            val anulado = "N"

            val now = LocalDateTime.now()
            val fecha = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val hora  = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

            val db = DatabaseApplication.getDatabase(requireContext())

            if (modo == "editar") {
                // Modo edición: actualizamos el pedido existente
                val pedidoId = activity?.intent?.getIntExtra("pedidoId", -1) ?: -1
                if (pedidoId != -1) {
                    // Obtenemos el encabezado actual (asegúrate de que getPedidoPrinteById() devuelva el objeto completo)
                    val existingHdr: PedidoHdr = db.PedidoHdrDAO().getPedidoPrinteById(pedidoId)
                    // Actualizamos el encabezado manteniendo, por ejemplo, el código original
                    val updatedHdr = existingHdr.copy(
                        tipopago = tipoPago,
                        subtotal = subtotal,
                        descuento = descuento,
                        total = total,
                        sinc = false,
                        impuesto = impuesto,
                        anulado = anulado,
                        descuentoRutaActivado = isDescuentoRutaActivado,
                        descuentoEscalaActivado = isDescuentoEscalaActivado,
                        descuentoTipoPagoActivado = isDescuentoTipoPagoActivado

                    )
                    db.PedidoHdrDAO().updatePedidoHdr(updatedHdr)

                    // Eliminamos los detalles existentes
                    db.PedidoDtlDAO().deletedtlid(pedidoId)

                    // Insertamos nuevamente los detalles actuales
                    detalleItems.forEach { item ->
                        val pedidoDtl = PedidoDtl(
                            idhdr = pedidoId,
                            codigoproducto = item.codigoproducto,
                            cantidad = item.quantity,
                            precio = item.price,
                            descuento = item.descuento,
                            nombre = item.nombreproducto,
                            impuesto = item.valorimpuesto,
                            porcentajeimpuesto = item.porcentajeImpuesto
                        )
                        db.PedidoDtlDAO().insertPedidoDtl(pedidoDtl)
                    }

                    withContext(Dispatchers.Main) {
                        // Cambiar estado y actualizar UI
                        isPedidoGuardado = true
                        view?.findViewById<ImageButton>(R.id.btnCancelPedido)?.isEnabled = false
                        btnsavepedido.text = "Salir"
                        btnsavepedido.setBackgroundColor(resources.getColor(R.color.yellow))
                        btnsavepedido.setOnClickListener { requireActivity().finish() }
                        showDialogAfterSave(pedidoId)
                    }
                }
            } else {
                // Modo creación: se inserta un nuevo pedido (código existente)
                val codigopedido = generateCodigoPedido()
                val pedidoHdr = PedidoHdr(
                    tipopago = tipoPago,
                    subtotal = subtotal,
                    descuento = descuento,
                    total = total,
                    sinc = false,
                    clientecodigo = clientecodigo,
                    impuesto = impuesto,
                    codigopedido = codigopedido,
                    anulado = anulado,
                    descuentoRutaActivado = isDescuentoRutaActivado,
                    descuentoEscalaActivado = isDescuentoEscalaActivado,
                    descuentoTipoPagoActivado = isDescuentoTipoPagoActivado
                )
                val hdrId = db.PedidoHdrDAO().insertPedidoHdr(pedidoHdr)
                if (hdrId > 0) {
                    detalleItems.forEach { item ->
                        val pedidoDtl = PedidoDtl(
                            idhdr = hdrId.toInt(),
                            codigoproducto = item.codigoproducto,
                            cantidad = item.quantity,
                            precio = item.price,
                            descuento = item.descuento,
                            nombre = item.nombreproducto,
                            impuesto = item.valorimpuesto,
                            porcentajeimpuesto = item.porcentajeImpuesto
                        )
                        db.PedidoDtlDAO().insertPedidoDtl(pedidoDtl)
                    }
                    withContext(Dispatchers.Main) {
                        isPedidoGuardado = true
                        view?.findViewById<ImageButton>(R.id.btnCancelPedido)?.isEnabled = false
                        btnsavepedido.text = "Salir"
                        btnsavepedido.setBackgroundColor(resources.getColor(R.color.yellow))
                        btnsavepedido.setOnClickListener { requireActivity().finish() }
                        showDialogAfterSave(hdrId.toInt())
                    }
                }
            }
        }
    }






    private fun showDialogAfterSave(pedidoId: Int) {
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setTitle("Impresión de Pedido")
            dialogBuilder.setMessage("¿Desea imprimir el pedido?")

            val dialog = dialogBuilder.create()

            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Sí") { _, _ ->
                printpedido(pedidoId)
                dialog.dismiss()

                // Desactivar switches después de imprimir
                view?.findViewById<SwitchCompat>(R.id.switchOption1)?.isEnabled = false
                view?.findViewById<SwitchCompat>(R.id.switchOption2)?.isEnabled = false
                view?.findViewById<SwitchCompat>(R.id.switchOption3)?.isEnabled = false

                // Desactivar ítems
                SharedDataModel.detalleItems.value?.forEach { it.isEnabled = false }
                SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)

                // Permitir salir del fragmento después de imprimir
                isPedidoGuardado = true
            }

            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancelar") { _, _ ->
                dialog.dismiss()
                requireActivity().finish()
                // Limpiar la lista de ítems
                SharedDataModel.detalleItems.postValue(mutableListOf())
            }

            dialog.setCancelable(false)
            dialog.show()
        }


        fun convertHtmlToPdf(htmlContent: String): ByteArrayOutputStream {
            val outputStream = ByteArrayOutputStream()
            val document = Document()
            try {
                val pdfWriter = PdfWriter.getInstance(document, outputStream)
                document.open()
                XMLWorkerHelper.getInstance().parseXHtml(
                    pdfWriter, document,
                    ByteArrayInputStream(htmlContent.toByteArray(StandardCharsets.UTF_8))
                )
            } finally {
                document.close()
            }
            Log.d("PDF Creation", "PDF byte length: ${outputStream.size()}")
            return outputStream
        }

        fun savePdfToFile(context: Context, pdfStream: ByteArrayOutputStream, fileName: String) {
            val file = File(context.getExternalFilesDir(null), fileName)
            try {
                FileOutputStream(file).use { fileOutputStream ->
                    pdfStream.writeTo(fileOutputStream)
                    Log.d("PDF Creation", "PDF saved to ${file.absolutePath}")
                }
            } catch (e: Exception) {
                Log.e("PDF Creation", "Error saving PDF", e)
            }
        }

        fun openPdfWithExternalViewer(context: Context, file: File) {
            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/pdf")
                flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        }

        fun printpedido(pedidoId: Int) {
            val fechaEmision = SimpleDateFormat("dd/MM/yyyy").format(Date())
            CoroutineScope(Dispatchers.IO).launch {
                val db = context?.let { DatabaseApplication.getDatabase(it) }
                db?.let {
                    val pedido = db.PedidoHdrDAO().getPedidoPrinteById(pedidoId)
                    val cliente = db.ClientesDAO().getClientById(pedido.clientecodigo)
                    val agente = db.AgenteDAO().getAgente()
                    val pedidoinfo = PedidoPrintModel(
                        pedidoId = pedido.id,
                        fechaEmision = Date().toString(),
                        tipoventa = pedido.tipopago,
                        clientenombre = cliente.nombrecliente ?: "",
                        codigocliente = cliente.Codigocliente ?: "",
                        rtncliente = cliente.Rtncliente ?: "",
                        rutanombre = agente.rutadesc,
                        vendedornombre = agente.descripcionCorta ?: "",
                        codigopedido = pedido.codigopedido
                    )
                    val df = DecimalFormat("#.##")
                    df.roundingMode = RoundingMode.FLOOR
                    val details = db.PedidoDtlDAO().getDetallePrint(pedidoId)
                    val subtotal = BigDecimal(pedido.subtotal).setScale(2, RoundingMode.HALF_UP).toDouble()
                    val descuento = Math.round(pedido.descuento * 100.0) / 100.0
                    val impuesto = Math.round(pedido.impuesto * 100.0) / 100.0
                    val total = (((pedido.subtotal + pedido.impuesto  - pedido.descuento) * 100.0).roundToLong() / 100.0)
                    val tableRows = generateTableRows(details)
                    val numeroletras = NumeroLetras.Convertir(
                        total.toString(),
                        "Lempira",
                        "Lempiras",
                        " ",
                        "centavos",
                        "con",
                        true
                    )
                    val htmlContent = pedidoinfo?.let { ped ->
                        HtmlTemplates.getHtmlForPdf(
                            ped.codigopedido, fechaEmision,
                            ped.tipoventa, ped.clientenombre, ped.codigocliente, ped.rtncliente, ped.rutanombre, ped.vendedornombre,
                            tableRows, subtotal, descuento, total, numeroletras, impuesto
                        )
                    }
                    val pdfStream = htmlContent?.let { it1 -> convertHtmlToPdf(it1) }
                    val fileName = "Pedido_#${pedido.codigopedido}.pdf"
                    if (pdfStream != null) {
                        savePdfToFile(requireContext(), pdfStream, fileName)
                    }
                    val file = File(requireContext().getExternalFilesDir(null), fileName)
                    openPdfWithExternalViewer(requireContext(), file)
                }
            }
        }

        fun configureSwitchColors(switch: SwitchCompat, context: Context) {
            // Colores personalizados para los estados activado y desactivado
            val verdeClaro = ContextCompat.getColor(context, R.color.verde_roca) // Verde claro para activado
            val verdeOscuro = ContextCompat.getColor(context, R.color.verde_roca_oscuro) // Verde oscuro para activado
            val grisClaro = ContextCompat.getColor(context, R.color.grayLight) // Gris claro para desactivado
            val grisOscuro = ContextCompat.getColor(context, R.color.grayDark) // Gris oscuro para desactivado

            // Crear ColorStateList para el "thumb" (el botón deslizable)
            val thumbColors = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked), // Estado activado
                    intArrayOf(-android.R.attr.state_checked) // Estado desactivado
                ),
                intArrayOf(
                    verdeClaro, // Color activado
                    grisOscuro  // Color desactivado
                )
            )

            // Crear ColorStateList para el "track" (la barra de fondo)
            val trackColors = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked), // Estado activado
                    intArrayOf(-android.R.attr.state_checked) // Estado desactivado
                ),
                intArrayOf(
                    verdeOscuro, // Color activado
                    grisClaro    // Color desactivado
                )
            )

            // Aplicar los colores al SwitchCompat
            switch.thumbTintList = thumbColors
            switch.trackTintList = trackColors
        }
}

