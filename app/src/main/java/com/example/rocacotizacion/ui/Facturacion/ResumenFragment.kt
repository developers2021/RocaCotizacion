
package com.example.rocacotizacion.ui.Facturacion

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.DAO.PedidoDtl
import com.example.rocacotizacion.DAO.PedidoHdr
import com.example.rocacotizacion.DTO.SharedDataModel
import com.example.rocacotizacion.DataModel.PedidoPrintModel
import com.example.rocacotizacion.R
import com.example.rocacotizacion.ui.PrintClass.HtmlTemplates
import com.example.rocacotizacion.ui.PrintClass.generateTableRows
import com.example.rocacotizacion.ui.PrintUtility.NumeroLetras
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
import java.text.SimpleDateFormat
import java.util.Date

class ResumenFragment : Fragment() {
    private lateinit var resumenAdapter: ResumenAdapter
    private var isEscalaDiscountEnabled: Boolean = false

    private fun applyEscalaDiscounts() {
        CoroutineScope(Dispatchers.IO).launch {
            SharedDataModel.detalleItems.value?.forEach { item ->
                val escalaDiscounts = DatabaseApplication.getDatabase(requireContext())
                    .invdescuentoporescalaDAO()
                    .getDescuentoPorEscala(item.codigoproducto)
                val escalaDiscount = escalaDiscounts.firstOrNull {
                    item.quantity >= it.rangoinicial && item.quantity <= it.rangofinal
                }
                item.porcentajeEscala= escalaDiscount?.monto ?: (0.0 / 100)
                item.porcentajeTotal=item.porcentajeEscala+item.porcentajeTipoPago+item.porcentajeRuta
                item.descuento=(item.price*item.quantity)*(item.porcentajeTotal/100)
                item.subtotal=(item.price*item.quantity)-item.descuento
               item.checkedDescuentoEscala=true
            }
            SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)
            withContext(Dispatchers.Main) {
                updateTotals()
            }
        }
    }
    private fun applyTipoVentaDiscounts() {
        CoroutineScope(Dispatchers.IO).launch {
            SharedDataModel.detalleItems.value?.forEach { item ->
                val discountData = DatabaseApplication.getDatabase(requireContext())
                    .invdescuentoportipoventaDAO()
                    .getDescuentoPorTipoVenta(item.codigoproducto)
                    item.porcentajeTipoPago= discountData?.monto ?: (0.0 / 100)
                    item.porcentajeTotal=item.porcentajeEscala+item.porcentajeTipoPago+item.porcentajeRuta
                    item.descuento=(item.price*item.quantity)*(item.porcentajeTotal/100)
                    item.subtotal=(item.price*item.quantity)-item.descuento

                item.checkedDescuentoTipoPago=true
            }
            SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)
            withContext(Dispatchers.Main) {
                updateTotals()
            }
        }
    }

    private fun applyRutaDiscounts() {
        CoroutineScope(Dispatchers.IO).launch {
            SharedDataModel.detalleItems.value?.forEach { item ->
                val idruta = DatabaseApplication.getDatabase(requireContext())
                    .AgenteDAO()
                    .getAgente()
                val discountData = DatabaseApplication.getDatabase(requireContext())
                    .invdescuentoporrutaDAO()
                    .getDescuentoPorRuta(idruta.idruta)
                    item.porcentajeRuta= discountData?.monto ?: (0.0 / 100)
                    item.porcentajeTotal=item.porcentajeEscala+item.porcentajeRuta+item.porcentajeTipoPago
                    item.descuento=(item.price*item.quantity)*(item.porcentajeTotal/100)
                    item.subtotal=(item.price*item.quantity)-item.descuento
                    item.checkedDescuentoRuta=true
        }
            SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)
            withContext(Dispatchers.Main) {
                updateTotals()
            }
    }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val switchEscala = view.findViewById<SwitchCompat>(R.id.switchOption1)
        val switchTipoPago = view.findViewById<SwitchCompat>(R.id.switchOption2)
        val switchRuta = view.findViewById<SwitchCompat>(R.id.switchOption3)

        switchEscala.setOnCheckedChangeListener { _, isChecked ->
            isEscalaDiscountEnabled = isChecked
            if (isChecked) {
                applyEscalaDiscounts()
            } else {
                removeEscalaDiscounts()
                updateTotals()
            }
        }

        switchTipoPago.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                applyTipoVentaDiscounts()
            } else {
                SharedDataModel.detalleItems.value?.forEach {
                    it.porcentajeTotal=it.porcentajeEscala+it.porcentajeRuta
                    it.porcentajeTipoPago = 0.0
                    it.descuento=it.price*it.quantity*(it.porcentajeEscala/100)*(it.porcentajeRuta/100)
                    it.subtotal=(it.price*it.quantity)-it.descuento
                    it.checkedDescuentoTipoPago=false
                }
                SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)
                updateTotals()
            }
        }
        switchRuta.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                applyRutaDiscounts()
            }
            else {
                SharedDataModel.detalleItems.value?.forEach {
                    it.porcentajeTotal=it.porcentajeEscala+it.porcentajeTipoPago
                    it.porcentajeRuta = 0.0
                    it.descuento=it.price*it.quantity*(it.porcentajeEscala/100)*(it.porcentajeTipoPago/100)
                    it.subtotal=(it.price*it.quantity)-it.descuento
                    it.checkedDescuentoRuta=false
                }
                SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)
                updateTotals()
            }
        }
    }
    private fun removeEscalaDiscounts() {
        SharedDataModel.detalleItems.value?.forEach {
            it.porcentajeTotal=it.porcentajeTipoPago+it.porcentajeRuta
            it.porcentajeEscala = 0.0
            it.descuento=it.price*it.quantity*(it.porcentajeTotal/100)
            it.subtotal=it.price*it.quantity-it.descuento
            it.checkedDescuentoEscala=false
            //it.checkedDescuentoTipoPago=false
        }
        SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)
    }
    private fun updateTotals() {
        val items = SharedDataModel.detalleItems.value ?: return
        val total = items.sumOf { it.subtotal }
        view?.findViewById<TextView>(R.id.sumtotal)?.text = "L.${String.format("%.2f", total)}"
        view?.findViewById<TextView>(R.id.sumsubtotal)?.text = "L.${String.format("%.2f", total)}"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_resumen, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewResumen1)
        resumenAdapter = ResumenAdapter(listOf())
        recyclerView.adapter = resumenAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        var textViewtipopago:TextView=view.findViewById(R.id.tipopago)
        textViewtipopago.text= activity?.intent?.getStringExtra("tipoPago")
            ?.let { stringtipopago(it) }
        // Observe the detalleItems LiveData
        SharedDataModel.detalleItems.observe(viewLifecycleOwner, Observer { items ->
            resumenAdapter.updateItems(items)
            // Update subtotal and total
            val total = items.sumOf { it.subtotal }
            val roundedTotal = String.format("%.2f", total)
            view.findViewById<TextView>(R.id.sumsubtotal).text = "L.$roundedTotal"
            view.findViewById<TextView>(R.id.sumtotal).text = "L.$roundedTotal"
        })
        //accion del boton guardar btnsavepedido
        val btnsavepedido: Button = view.findViewById(R.id.btnsavepedido)
        btnsavepedido.setOnClickListener {
            if (SharedDataModel.detalleItems.value.isNullOrEmpty()) {
                Toast.makeText(context, "No hay items en el pedido", Toast.LENGTH_SHORT).show()
            } else {
                saveOrder()
            }
        }

        return view
    }
    fun stringtipopago(tipopago: String):String{
        if (tipopago=="CRED")
            return "Credito"
        else
            return "Contado"
    }
    private fun saveOrder() {
        CoroutineScope(Dispatchers.IO).launch {
            val tipoPago = activity?.intent?.getStringExtra("tipoPago") ?: "Contado"
            val clientecodigo =  activity?.intent?.getStringExtra("clientecodigo")?:"000"
            val detalleItems = SharedDataModel.detalleItems.value ?: listOf()

            val subtotal = detalleItems.sumOf { it.subtotal }
            val descuento =detalleItems.sumOf { it.descuento } // Calculate any discount if applicable

            val pedidoHdr = PedidoHdr(tipopago = tipoPago, subtotal = subtotal, descuento = descuento, total = subtotal, sinc = false, clientecodigo =clientecodigo )
            val hdrId = DatabaseApplication.getDatabase(requireContext()).PedidoHdrDAO().insertPedidoHdr(pedidoHdr)

            if (hdrId > 0) {
                // Insert was successful, now insert the details
                detalleItems.forEach { item ->
                    val pedidoDtl = PedidoDtl(
                        idhdr = hdrId.toInt(),
                        codigoproducto = item.codigoproducto,
                        cantidad = item.quantity,
                        precio = item.price,
                        descuento = item.descuento,
                        nombre = item.nombreproducto,
                    )
                    DatabaseApplication.getDatabase(requireContext()).PedidoDtlDAO().insertPedidoDtl(pedidoDtl)
                }



                // Post the action to the Main thread to handle UI
                withContext(Dispatchers.Main) {
                    showDialogAfterSave(hdrId.toInt())
                }
            }
        }
    }
    private fun showDialogAfterSave(pedidoId: Int) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("Impresión de Pedido")
        dialogBuilder.setMessage("¿Desea imprimir el pedido?")

        // Create the dialog once here and manage its dismissal manually.
        val dialog = dialogBuilder.create()

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Sí") { _, _ ->
            printpedido(pedidoId)
            dialog.dismiss()
            view?.findViewById<Button>(R.id.btnsavepedido)?.let { btnsavepedido ->
                btnsavepedido.text = "Imprimir Pedido"
                    btnsavepedido.setOnClickListener {
                        printpedido(pedidoId)
                    }
                }
            view?.findViewById<Button>(R.id.switchOption1)?.let { switchOption1 ->
                switchOption1.isEnabled = false
            }
            view?.findViewById<Button>(R.id.switchOption2)?.let { switchOption2 ->
                switchOption2.isEnabled = false
            }
            view?.findViewById<Button>(R.id.switchOption3)?.let { switchOption3 ->
                switchOption3.isEnabled = false
            }
            //define en false cuando ya fue guardado el pedido
            SharedDataModel.detalleItems.value?.forEach { it.isEnabled = false }
            SharedDataModel.detalleItems.postValue(SharedDataModel.detalleItems.value)
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancelar") { _, _ ->
            // Dismiss the dialog and go back to the previous fragment.
            dialog.dismiss()
            requireActivity().onBackPressed()
            // Clear the SharedDataModel.detalleItems list
            SharedDataModel.detalleItems.postValue(mutableListOf())


        }

        dialog.setCancelable(false)  // Prevents cancelling the dialog by tapping outside or pressing back.
        dialog.show()
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
        var pedidoinfo: PedidoPrintModel
        CoroutineScope(Dispatchers.IO).launch {
            val db = context?.let { DatabaseApplication.getDatabase(it) }
            db?.let {
                val pedido= db.PedidoHdrDAO().getPedidoPrinteById(pedidoId)
                val cliente=db.ClientesDAO().getClientById(pedido.clientecodigo)
                val agente=db.AgenteDAO().getAgente()
                pedidoinfo= PedidoPrintModel(
                    pedidoId=pedido.id,
                    fechaEmision = Date().toString(),
                    tipoventa = pedido.tipopago,
                    clientenombre = cliente.nombrecliente?:"",
                    codigocliente = cliente.Codigocliente?:"",
                    rtncliente = cliente.Rtncliente?:"",
                    rutanombre = agente.rutadesc,
                    vendedornombre = agente.descripcionCorta?:""
                )
                val df = DecimalFormat("#.##")
                df.roundingMode = RoundingMode.FLOOR
                val details =db.PedidoDtlDAO().getDetallePrint(pedidoId)
                val total=Math.round(pedido.subtotal*100.00)/100.00
                val subtotal=Math.round((pedido.subtotal+pedido.descuento)*100.00)/100.00

                val tableRows = generateTableRows(details)
                val numeroletras= NumeroLetras.Convertir(total.toString(),"Lempira","Lempiras"," ","centavos","con",true)
                val htmlContent = pedidoinfo?.let { ped ->
                    HtmlTemplates.getHtmlForPdf(pedidoId.toString(), fechaEmision,
                        ped.tipoventa,ped.clientenombre,ped.codigocliente,ped.rtncliente,ped.rutanombre,ped.vendedornombre ,
                        tableRows,subtotal,pedido.descuento,total,numeroletras)
                }
                val pdfStream = htmlContent?.let { it1 -> convertHtmlToPdf(it1) }
                val fileName = "Pedido_#$pedidoId.pdf"
                if (pdfStream != null) {
                    savePdfToFile(requireContext(), pdfStream, fileName)
                }
                val file = File(requireContext().getExternalFilesDir(null), fileName)
                openPdfWithExternalViewer(requireContext(), file)
            }
        }
    }
}