package com.example.rocacotizacion.ui.Facturacion

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.rocacotizacion.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.lifecycle.ViewModelProvider
import com.example.rocacotizacion.DataModel.PedidoViewModel


class FacturacionFragment : Fragment() {

    lateinit var viewPager: ViewPager2
    private lateinit var tabs: TabLayout
    private lateinit var pedidoViewModel: PedidoViewModel

    companion object {
        /**
         * Instancia el fragmento en modo edición.
         * Se pasa el pedidoId junto con los datos del cliente y tipo de pago.
         */
        fun newInstanceEditMode(
            pedidoId: Int,
            tipoPago: String?,
            clienteNombre: String?,
            clientecodigo: String?
        ): FacturacionFragment {
            val fragment = FacturacionFragment()
            val bundle = Bundle().apply {
                putString("modo", "editar")
                putInt("pedidoId", pedidoId)
                putString("tipoPago", tipoPago)
                putString("clienteNombre", clienteNombre)
                putString("clientecodigo", clientecodigo)
            }
            fragment.arguments = bundle
            return fragment
        }

        /**
         * Instancia el fragmento en modo creación.
         */
        fun newInstanceCreateMode(
            tipoPago: String?,
            clienteNombre: String?,
            clientecodigo: String?
        ): FacturacionFragment {
            val fragment = FacturacionFragment()
            val bundle = Bundle().apply {
                putString("modo", "crear")
                putString("tipoPago", tipoPago)
                putString("clienteNombre", clienteNombre)
                putString("clientecodigo", clientecodigo)
            }
            fragment.arguments = bundle
            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_facturacion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa ViewPager2 y TabLayout
        viewPager = view.findViewById(R.id.viewpager_facturacion)
        tabs = view.findViewById(R.id.tabs_facturacion)
        pedidoViewModel = ViewModelProvider(this).get(PedidoViewModel::class.java)

        val tipoPago = arguments?.getString("tipoPago")
        val clienteNombre = arguments?.getString("clienteNombre")
        val clientecodigo = arguments?.getString("clientecodigo")
        val modo = arguments?.getString("modo") // "editar" o "crear"

        // (Si modo = editar, se carga pedidoId y se llama a loadPedido)
        var pedidoId: Int? = null
        if (modo == "editar") {
            pedidoId = arguments?.getInt("pedidoId") ?: -1
            if (pedidoId != -1) {
                pedidoViewModel.loadPedido(pedidoId)
            }
        }

        val bundle = Bundle().apply {
            putString("tipoPago", tipoPago)
            putString("clienteNombre", clienteNombre)
            putString("clientecodigo", clientecodigo)
            if (modo == "editar" && pedidoId != null && pedidoId != -1) {
                putInt("pedidoId", pedidoId)
            }
        }

        val pagerAdapter = FacturacionPagerAdapter(this, bundle)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Detalles"
                1 -> "Resumen"
                else -> null
            }
        }.attach()

        // 1) Arrancamos explícitamente en tab 0 (Detalles)
        viewPager.setCurrentItem(0, false)

        if (modo == "editar") {
            viewPager.post {
                // Esto se ejecuta un instante después, saltando a la pestaña Resumen:
                viewPager.setCurrentItem(1, false)
            }
        }

    }

    fun goToTab(position: Int) {
        viewPager.setCurrentItem(position, false)
    }

    /**
     * Adaptador para el ViewPager2 que muestra dos fragmentos:
     * - DetalleFragment: muestra los datos y permite editar los detalles del pedido.
     * - ResumenFragment: muestra el resumen del pedido.
     */
    class FacturacionPagerAdapter(
        fragment: Fragment,
        private val bundle: Bundle
    ) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DetalleFragment().apply { arguments = bundle }
                1 -> ResumenFragment().apply { arguments = bundle }
                else -> throw IllegalStateException("Unexpected position $position")
            }
        }
    }
}
