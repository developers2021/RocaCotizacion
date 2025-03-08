package com.example.rocacotizacion.ui.Facturacion

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DTO.SharedDataModel
import com.example.rocacotizacion.DataModel.DetallesAdapter
import com.example.rocacotizacion.DataModel.DetalleItem
import com.example.rocacotizacion.DataModel.PedidoViewModel
import com.example.rocacotizacion.R
import com.example.rocacotizacion.ui.ListaProducto.ListaProductoActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DetalleFragment : Fragment(), DetallesAdapter.OnItemCloseClickListener {

    private lateinit var detallesAdapter: DetallesAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var pedidoViewModel: PedidoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        // Observador general para SharedDataModel: cada vez que cambie la lista de DetalleItems
        SharedDataModel.detalleItems.observe(viewLifecycleOwner, Observer { items ->
            detallesAdapter.updateDetalles(items)
            // FAB siempre visible para agregar
            fab.visibility = View.VISIBLE
        })

        return view
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Toast.makeText(context, "No puedes volver atrás en este paso.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Verificamos si es modo edición (pedidoId != -1)
        val pedidoId = arguments?.getInt("pedidoId") ?: -1
        if (pedidoId != -1) {
            // Modo edición
            pedidoViewModel = ViewModelProvider(requireActivity()).get(PedidoViewModel::class.java)
            pedidoViewModel.loadPedido(pedidoId)

            // Observa el LiveData con los detalles del pedido
            pedidoViewModel.pedidoDtlLiveData.observe(viewLifecycleOwner, Observer { details ->
                if (!details.isNullOrEmpty()) {
                    // Mapeamos cada registro de la BD (PedidoDtl) a DetalleItem
                    val detalleItems = details.map { detalle ->

                        // Verificamos si YA tenemos un item en SharedDataModel con ese codigoproducto
                        val existingItem = SharedDataModel.detalleItems.value
                            ?.firstOrNull { it.codigoproducto == detalle.codigoproducto }

                        // Subtotal base
                        val subtotalCalc = existingItem?.subtotal
                            ?: (detalle.precio * detalle.cantidad)

                        // Descuento base
                        val descuentoCalc = existingItem?.descuento
                            ?: detalle.descuento

                        // Monto de impuesto que la BD tenía calculado
                        val valorImpuestoCalc = existingItem?.valorimpuesto
                            ?: detalle.impuesto

                        // Verificar el % de la BD y forzar a 15% si estaba en 0.0
                        val porcentajeImpuestoFromDb = detalle.porcentajeimpuesto

                        // Decidir el % final: si ya existía un item en memoria, heredar su porcentaje
                        // o si no, usar el que viene de la BD
                        val finalPorcentajeImpuesto = existingItem?.porcentajeImpuesto
                            ?: porcentajeImpuestoFromDb

                        // Calcular total base
                        val totalCalc = existingItem?.total
                            ?: (subtotalCalc - descuentoCalc + valorImpuestoCalc)

                        DetalleItem(
                            quantity = detalle.cantidad,
                            price = detalle.precio,
                            subtotal = subtotalCalc,
                            nombreproducto = detalle.nombre,
                            codigoproducto = detalle.codigoproducto,
                            descuento = descuentoCalc,

                            // Flags de descuentos: si existía el item en memoria, usamos los suyos
                            checkedDescuentoEscala = existingItem?.checkedDescuentoEscala ?: false,
                            checkedDescuentoTipoPago = existingItem?.checkedDescuentoTipoPago ?: false,
                            checkedDescuentoRuta = existingItem?.checkedDescuentoRuta ?: false,

                            descuentoTotal = existingItem?.descuentoTotal ?: 0.0,
                            porcentajeEscala = existingItem?.porcentajeEscala ?: 0.0,
                            porcentajeTipoPago = existingItem?.porcentajeTipoPago ?: 0.0,
                            porcentajeRuta = existingItem?.porcentajeRuta ?: 0.0,
                            porcentajeTotal = existingItem?.porcentajeTotal ?: 0.0,

                            // Asignamos el valor de impuesto y el porcentaje final
                            valorimpuesto = valorImpuestoCalc,
                            porcentajeImpuesto = finalPorcentajeImpuesto,

                            // Control de edición
                            isEnabled = existingItem?.isEnabled ?: true,
                            total = totalCalc
                        )
                    }

                    // Asignamos la lista mapeada al SharedDataModel y actualizamos el adapter
                    SharedDataModel.detalleItems.postValue(detalleItems.toMutableList())
                    detallesAdapter.updateDetalles(detalleItems)
                }
            })
        } else {
            // Modo creación (pedidoId == -1)
            // No se hace carga desde BD, se usa el flujo normal de SharedDataModel.
        }
    }

    /**
     * Método del adapter para eliminar un ítem al hacer click en el botón "Close".
     */
    override fun onItemCloseClick(position: Int) {
        SharedDataModel.detalleItems.value?.let { items ->
            val updatedItems = items.toMutableList()
            updatedItems.removeAt(position)
            SharedDataModel.detalleItems.postValue(updatedItems)
        }
    }

    private fun setStatusBarColor(context: Context) {
        val window = (context as Activity).window
        window.statusBarColor = ContextCompat.getColor(context, R.color.grayDark)
    }
}
