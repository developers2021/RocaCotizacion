package com.example.rocacotizacion.ui.Facturacion

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DTO.SharedDataModel
import com.example.rocacotizacion.DataModel.DetallesAdapter
import com.example.rocacotizacion.R
import com.example.rocacotizacion.ui.ListaProducto.ListaProductoActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat

class DetalleFragment : Fragment(), DetallesAdapter.OnItemCloseClickListener {
    private lateinit var detallesAdapter: DetallesAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalles, container, false)

        setStatusBarColor(requireContext())


        // Configuración del RecyclerView
        detallesAdapter = DetallesAdapter(mutableListOf(), this)
        recyclerView = view.findViewById(R.id.recyclerViewDetalles)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = detallesAdapter

        // Configuración del FloatingActionButton (FAB)
        fab = view.findViewById(R.id.fab_add)
        fab.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.verde_roca)
        fab.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
        fab.setOnClickListener {
            val intent = Intent(context, ListaProductoActivity::class.java)
            val tipoPago = arguments?.getString("tipoPago")
            intent.putExtra("tipoPago", tipoPago)
            startActivity(intent)
        }

        // Observador para actualizar el RecyclerView
        SharedDataModel.detalleItems.observe(viewLifecycleOwner, Observer { items ->
            detallesAdapter.updateDetalles(items)
            if (items.isNotEmpty()) {
                fab.visibility = if (items.first().isEnabled) View.VISIBLE else View.GONE
            } else {
                fab.visibility = View.VISIBLE // Mostrar el FAB si la lista está vacía
            }
        })

        return view
    }

    override fun onResume() {
        super.onResume()

        // Bloquear el botón de retroceso
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Mostrar mensaje y bloquear el botón de retroceso
                    Toast.makeText(context, "No puedes volver atrás en este paso.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    override fun onItemCloseClick(position: Int) {
        // Eliminar un elemento del RecyclerView y actualizar la lista compartida
        SharedDataModel.detalleItems.value?.let { items ->
            val updatedItems = items.toMutableList()
            updatedItems.removeAt(position)
            SharedDataModel.detalleItems.postValue(updatedItems)
        }
    }

    private fun setStatusBarColor(context: Context) {
        val window = (context as Activity).window
        window.statusBarColor = ContextCompat.getColor(context, R.color.grayDark) // Cambia el color según tu diseño
    }


}
