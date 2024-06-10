package com.example.rocacotizacion.ui.DetallePedido

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DAO.Clientes
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.DAO.PedidoHdr
import com.example.rocacotizacion.DataModel.PedidoPrintModel
import com.example.rocacotizacion.R
import com.example.rocacotizacion.ui.MiDia.PedidoViewModel
import com.example.rocacotizacion.ui.NumeroLetras.NumeroLetras
import com.example.rocacotizacion.ui.PrintClass.HtmlTemplates
import com.example.rocacotizacion.ui.PrintClass.ProductDetail
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

        viewModel = ViewModelProvider(this).get(PedidoViewModel::class.java)
        setupRecyclerView(view)

        viewModel.getDetailsByPedidoId(pedidoId).observe(viewLifecycleOwner) { details ->
            adapter.updateDetails(details)
        }

        viewModel.getPedidoHdrById(pedidoId).observe(viewLifecycleOwner) { hdr ->
            if (hdr != null) {
                val symbols =DecimalFormatSymbols(Locale.US) // Ensure the locale is set to US for comma separators
                symbols.setGroupingSeparator(',')
                symbols.setDecimalSeparator('.')
                val df = DecimalFormat("#,##0.00", symbols)
                view.findViewById<TextView>(R.id.sumsubtotal).text =  df.format(hdr.subtotal + hdr.descuento)
                view.findViewById<TextView>(R.id.sumtotal).text =  df.format(hdr.total)
                var tipopago=""
                if (hdr.tipopago=="CTADO") {
                    tipopago="Contado"
                }
                else{tipopago="Credito"}
                view.findViewById<TextView>(R.id.tipopago).text = tipopago
                view.findViewById<TextView>(R.id.sumdescuento).text =  df.format(hdr.descuento)

            }
        }



        val buttonPrint = view.findViewById<ImageButton>(R.id.button_print)
        buttonPrint.setOnClickListener {
            val pedidoId = arguments?.getInt("pedidoId") ?: -1
            val fechaEmision = SimpleDateFormat("dd/MM/yyyy").format(Date())

            var pedidoinfo: PedidoPrintModel
            CoroutineScope(Dispatchers.IO).launch {
                val db = context?.let { DatabaseApplication.getDatabase(it) }
                db?.let {
                    val pedido= db.PedidoHdrDAO().getPedidoPrinteById(pedidoId)
                    val cliente=db.ClientesDAO().getClientById(pedido.clientecodigo)
                    val agente=db.AgenteDAO().getAgente()
                    pedidoinfo=PedidoPrintModel(
                        pedidoId=pedido.id,
                        fechaEmision = Date().toString(),
                        tipoventa = pedido.tipopago,
                        clientenombre = cliente.nombrecliente?:"",
                        codigocliente = cliente.Codigocliente?:"",
                        rtncliente = cliente.Rtncliente?:"",
                        rutanombre = agente.rutadesc,
                        vendedornombre = agente.descripcionCorta?:""
                    )


                    val details =db.PedidoDtlDAO().getDetallePrint(pedidoId)
                    val total=pedido.total

                    val tableRows = generateTableRows(details)
                    val df = DecimalFormat("#.##")
                    df.roundingMode = RoundingMode.FLOOR
                    val numeroletras= NumeroLetras.Convertir(df.format(total).toString(),"Lempira","Lempiras"," ","centavos","con",true)
                    val subtotal=pedido.subtotal+pedido.descuento
                    val decuento=pedido.descuento
                    val htmlContent = pedidoinfo?.let { ped ->
                        HtmlTemplates.getHtmlForPdf(pedidoId.toString(), fechaEmision,
                            ped.tipoventa,ped.clientenombre,ped.codigocliente,ped.rtncliente,ped.rutanombre,ped.vendedornombre ,
                            tableRows,subtotal,decuento,total,numeroletras)
                    }

                    val pdfStream = htmlContent?.let { it1 -> convertHtmlToPdf(it1) }
                    val fileName = "Pedido_$$pedidoId.pdf"
                    if (pdfStream != null) {
                        savePdfToFile(requireContext(), pdfStream, fileName)
                    }
                    val file = File(requireContext().getExternalFilesDir(null), fileName)
                    openPdfWithExternalViewer(requireContext(), file)
                }

            }
        }

        val deleteButton = view.findViewById<ImageButton>(R.id.button_delete)
        deleteButton.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Alerta")
            builder.setMessage("¿Está seguro que desea eliminar este pedido?")
            builder.setPositiveButton("Eliminar") { dialog, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    val db = context?.let { DatabaseApplication.getDatabase(it) }
                    db?.let {
                        it.PedidoHdrDAO().deletehdrid(pedidoId)
                        it.PedidoDtlDAO().deletedtlid(pedidoId)
                        // Re-fetch the list on IO thread
                        val updatedDetails = it.PedidoDtlDAO().getAllDetailsByHeaderId(pedidoId)
                        withContext(Dispatchers.Main) {
                            // Update the RecyclerView on the Main thread
                            adapter.updateDetails(updatedDetails)
                            Toast.makeText(context, "Pedido eliminado", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack() // Optionally go back
                        }
                    } ?: withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Database access error", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
            }
            builder.setNeutralButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }

    }

    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewResumen)
        adapter = PedidoDetailAdapter(listOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }


}
