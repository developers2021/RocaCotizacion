package com.example.rocacotizacion.ui.DetallePedido

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DAO.AppDatabase
import com.example.rocacotizacion.DAO.Clientes
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.R
import com.example.rocacotizacion.ui.MiDia.PedidoViewModel
import android.view.MenuItem
import android.widget.ImageButton
import com.example.rocacotizacion.ui.Facturacion.FacturacionActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetallePedidoFragment : Fragment() {

    private lateinit var viewModel: PedidoViewModel
    private lateinit var adapter: PedidoDetailAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detalle_pedido, container, false)
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
            Log.d("DetallePedidoFragment", "hdr received: $hdr")
            if (hdr != null) {
                view.findViewById<TextView>(R.id.sumsubtotal).text = hdr.subtotal.toString()
                view.findViewById<TextView>(R.id.sumtotal).text = hdr.total.toString()
                view.findViewById<TextView>(R.id.tipopago).text = hdr.tipopago
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
