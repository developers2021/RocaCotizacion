package com.example.rocacotizacion.ui.MiDia

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.ui.MiDia.PedidoSummaryAdapter
import com.example.rocacotizacion.DataModel.PedidoSummary
import com.example.rocacotizacion.R


class MiDiaPedidosFragment : Fragment() {

    private lateinit var viewModel: PedidoViewModel
    private lateinit var adapter: PedidoSummaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pedidos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar el ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(PedidoViewModel::class.java)

        // Configuración del RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewPedidos)

        // Crear el adaptador con el clickListener para manejar el clic en cada item
        adapter = PedidoSummaryAdapter(mutableListOf()) { pedidoSummary ->
            // Llama a la función para navegar al fragmento de detalle del pedido
            navigateToDetallePedido(pedidoSummary.id)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observar los datos del ViewModel
        viewModel.pedidoHdrList.observe(viewLifecycleOwner) { pedidoHdrList ->
            val pedidoSummaryList = pedidoHdrList.map { hdr ->
                PedidoSummary(hdr.id, hdr.codigopedido, hdr.tipopago, hdr.total, hdr.sinc, hdr.clientecodigo, hdr.anulado)
            }
            adapter.updateItems(pedidoSummaryList)
        }
    }

    // Función para navegar al detalle del pedido
    private fun navigateToDetallePedido(pedidoId: Int) {
        try {
            (requireParentFragment() as MiDiaFragment).navigateToDetallePedidoFromMiDia(pedidoId)
        } catch (e: Exception) {
            Log.e("MiDiaPedidosFragment", "Error al navegar: ${e.message}")
            Toast.makeText(requireContext(), "Error al navegar al detalle del pedido", Toast.LENGTH_SHORT).show()
        }
    }



}
