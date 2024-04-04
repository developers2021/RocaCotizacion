package com.example.rocacotizacion.ui.Facturacion

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DTO.SharedDataModel
import com.example.rocacotizacion.DataModel.DetallesAdapter
import com.example.rocacotizacion.R
import com.example.rocacotizacion.ui.ListaProducto.ListaProductoActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DetalleFragment : Fragment(), DetallesAdapter.OnItemCloseClickListener {
    private lateinit var detallesAdapter: DetallesAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalles, container, false)

        detallesAdapter = DetallesAdapter(SharedDataModel.detalleItems, this)
        recyclerView = view.findViewById(R.id.recyclerViewDetalles)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = detallesAdapter

        val fab: FloatingActionButton = view.findViewById(R.id.fab_add)
        fab.setOnClickListener {
            val intent = Intent(context, ListaProductoActivity::class.java)
            val tipoPago = arguments?.getString("tipoPago")
            intent.putExtra("tipoPago", tipoPago)
            startActivity(intent)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        detallesAdapter.updateDetalles(SharedDataModel.detalleItems)
    }

    override fun onItemCloseClick(position: Int) {
        SharedDataModel.detalleItems.removeAt(position)
        detallesAdapter.notifyItemRemoved(position)
    }
}
