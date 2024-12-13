package com.example.rocacotizacion.ui.DetallePedido

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.DataModel.PedidoPrintModel
import com.example.rocacotizacion.R
import com.example.rocacotizacion.ui.MiDia.PedidoViewModel
import com.example.rocacotizacion.ui.PrintUtility.NumeroLetras
import com.example.rocacotizacion.ui.PrintClass.HtmlTemplates
import com.example.rocacotizacion.ui.PrintClass.generateTableRows
import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.tool.xml.XMLWorkerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.math.RoundingMode
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class DetallePedidoFragment : Fragment() {

    private lateinit var viewModel: PedidoViewModel
    private lateinit var adapter: PedidoDetailAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detalle_pedido, container, false)
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

    fun convertHtmlToPdf(htmlContent: String): ByteArrayOutputStream {
        val outputStream = ByteArrayOutputStream()
        val document = Document()
        try {
            val pdfWriter = PdfWriter.getInstance(document, outputStream)
            document.open()
            XMLWorkerHelper.getInstance().parseXHtml(pdfWriter, document,
                ByteArrayInputStream(htmlContent.toByteArray(StandardCharsets.UTF_8))
            )
        } finally {
            document.close()
        }
        Log.d("PDF Creation", "PDF byte length: ${outputStream.size()}")
        return outputStream
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pedidoId = arguments?.getInt("pedidoId") ?: -1
        setHasOptionsMenu(true)

        // Validar el pedidoId antes de continuar
        if (pedidoId == -1) {
            Toast.makeText(requireContext(), "No se recibió un ID de pedido válido", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack() // Regresar si el ID es inválido
            return
        }

        viewModel = ViewModelProvider(this).get(PedidoViewModel::class.java)
        setupRecyclerView(view)

        // Observar detalles del pedido
        viewModel.getDetailsByPedidoId(pedidoId).observe(viewLifecycleOwner) { details ->
            adapter.updateDetails(details)
        }

        // Observar encabezado del pedido y actualizar los TextView
        viewModel.getPedidoHdrById(pedidoId).observe(viewLifecycleOwner) { hdr ->
            if (hdr != null) {
                val decimalFormat = DecimalFormat("#,##0.00")

                // Obtener y validar los TextView
                val sumSubtotal = view.findViewById<TextView>(R.id.sumsubtotal)
                val sumTotal = view.findViewById<TextView>(R.id.sumtotal)
                val sumImpuesto = view.findViewById<TextView>(R.id.sumimpuesto)
                val sumDescuento = view.findViewById<TextView>(R.id.sumdescuento)
                val tipoPagoText = view.findViewById<TextView>(R.id.tipopago)

                // Asignar valores si los TextView no son nulos
                sumSubtotal?.text = decimalFormat.format(hdr.subtotal)
                sumTotal?.text = decimalFormat.format(hdr.total)
                sumImpuesto?.text = decimalFormat.format(hdr.impuesto)
                sumDescuento?.text = decimalFormat.format(hdr.descuento)

                val tipopago = if (hdr.tipopago == "CTADO") "Contado" else "Credito"
                tipoPagoText?.text = tipopago
            } else {
                Toast.makeText(requireContext(), "No se encontró el encabezado del pedido", Toast.LENGTH_SHORT).show()
            }
        }

        // Configuración del botón de eliminación
        /*val deleteButton = view.findViewById<ImageButton>(R.id.button_delete)
        viewModel.getSincByPedidoId(pedidoId).observe(viewLifecycleOwner) { isSynchronized ->
            if (isSynchronized == true) {
                deleteButton.isVisible = false
            } else {
                deleteButton.setOnClickListener {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Alerta")
                        .setMessage("¿Está seguro que desea eliminar este pedido?")
                        .setPositiveButton("Eliminar") { dialog, _ ->
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val db = DatabaseApplication.getDatabase(requireContext())
                                    db.PedidoHdrDAO().deletehdrid(pedidoId)
                                    db.PedidoDtlDAO().deletedtlid(pedidoId)

                                    // Obtener la lista actualizada en el hilo IO
                                    val updatedDetails = db.PedidoDtlDAO().getAllDetailsByHeaderId(pedidoId)
                                    withContext(Dispatchers.Main) {
                                        // Actualizar la interfaz de usuario en el hilo principal
                                        adapter.updateDetails(updatedDetails)
                                        Toast.makeText(requireContext(), "Pedido eliminado", Toast.LENGTH_SHORT).show()
                                        parentFragmentManager.popBackStack()
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(requireContext(), "Error al acceder a la base de datos", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                dialog.dismiss()
                            }
                        }
                        .setNeutralButton("Cancelar") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }*/

        // Configuración del botón de anulación
        val deleteButton = view.findViewById<ImageButton>(R.id.button_delete)
        viewModel.getSincByPedidoId(pedidoId).observe(viewLifecycleOwner) { isSynchronized ->
            if (isSynchronized == true) {
                deleteButton.isVisible = false
            } else {
                deleteButton.setOnClickListener {
                    // Mostrar el diálogo para ingresar la clave
                    val builder = AlertDialog.Builder(requireContext())
                    val dialogView = layoutInflater.inflate(R.layout.dialog_anular_pedido, null)
                    val inputPassword = dialogView.findViewById<EditText>(R.id.editTextPassword)

                    builder.setView(dialogView)
                        .setTitle("Autenticación requerida")
                        .setMessage("Ingrese la clave para anular el pedido")
                        .setPositiveButton("Aceptar") { dialog, _ ->
                            val password = inputPassword.text.toString()
                            if (password == "L@roc@*2023") {
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val db = DatabaseApplication.getDatabase(requireContext())

                                        // Actualizar el estado del pedido a "Anulado"
                                        db.PedidoHdrDAO().updateAnuladoStatus(pedidoId, "S") // 'S' para anulado

                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(requireContext(), "Pedido anulado", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(requireContext(), "Error al anular el pedido", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    dialog.dismiss()
                                }
                            } else {
                                Toast.makeText(requireContext(), "Clave incorrecta", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Cancelar") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }



        // Configuración del botón de impresión
        val buttonPrint = view.findViewById<ImageButton>(R.id.button_print)
        buttonPrint.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = DatabaseApplication.getDatabase(requireContext())
                    val pedido = db.PedidoHdrDAO().getPedidoPrinteById(pedidoId)
                    val cliente = db.ClientesDAO().getClientById(pedido.clientecodigo)
                    val agente = db.AgenteDAO().getAgente()

                    val pedidoinfo = PedidoPrintModel(
                        pedidoId = pedido.id,
                        fechaEmision = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                        tipoventa = pedido.tipopago,
                        clientenombre = cliente.nombrecliente ?: "",
                        codigocliente = cliente.Codigocliente ?: "",
                        rtncliente = cliente.Rtncliente ?: "",
                        rutanombre = agente.rutadesc,
                        vendedornombre = agente.descripcionCorta ?: "",
                        codigopedido = pedido.codigopedido
                    )

                    val df = DecimalFormat("#.##").apply { roundingMode = RoundingMode.FLOOR }
                    val details = db.PedidoDtlDAO().getDetallePrint(pedidoId)
                    val subtotal = Math.round((pedido.subtotal + pedido.descuento) * 100.0) / 100.0
                    val impuesto = Math.round(pedido.impuesto * 100.0) / 100.0
                    val total = Math.round((pedido.subtotal + pedido.impuesto) * 100.0) / 100.0
                    val tableRows = generateTableRows(details)
                    val numeroletras = NumeroLetras.Convertir(total.toString(), "Lempira", "Lempiras", " ", "centavos", "con", true)

                    val htmlContent = HtmlTemplates.getHtmlForPdf(
                        pedido.codigopedido,
                        pedidoinfo.fechaEmision,
                        pedidoinfo.tipoventa,
                        pedidoinfo.clientenombre,
                        pedidoinfo.codigocliente,
                        pedidoinfo.rtncliente,
                        pedidoinfo.rutanombre,
                        pedidoinfo.vendedornombre,
                        tableRows,
                        subtotal,
                        pedido.descuento,
                        total,
                        numeroletras,
                        impuesto
                    )

                    val pdfStream = htmlContent?.let { convertHtmlToPdf(it) }
                    pdfStream?.let {
                        val fileName = "Pedido_#${pedido.codigopedido}.pdf"
                        savePdfToFile(requireContext(), it, fileName)
                        val file = File(requireContext().getExternalFilesDir(null), fileName)
                        openPdfWithExternalViewer(requireContext(), file)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error al generar el PDF", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }



    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewResumen)
        adapter = PedidoDetailAdapter(listOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }




}
