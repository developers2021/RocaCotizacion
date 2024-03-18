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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FacturacionFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabs: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_facturacion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(R.id.viewpager_facturacion)
        tabs = view.findViewById(R.id.tabs_facturacion)
        // Retrieve the arguments passed from FacturacionActivity
        val tipoPago = arguments?.getString("tipoPago")
        val clienteNombre = arguments?.getString("clienteNombre")
        // Initialize the ViewPager2 adapter
        val pagerAdapter = FacturacionPagerAdapter(this, tipoPago, clienteNombre)
        viewPager.adapter = pagerAdapter

        // Attach the TabLayout and ViewPager2 together
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Detalles"
                1 -> "Resumen"
                else -> null
            }
        }.attach()
    }

    class FacturacionPagerAdapter(
        fragment: Fragment,
        private val tipoPago: String?,
        private val clienteNombre: String?
    ) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            // Pass the arguments to the respective fragments
            val bundle = Bundle().apply {
                putString("tipoPago", tipoPago)
                putString("clienteNombre", clienteNombre)
            }
            return when (position) {
                0 -> DetalleFragment().apply { arguments = bundle }
                1 -> ResumenFragment().apply { arguments = bundle }
                else -> throw IllegalStateException("Unexpected position $position")
            }
        }
    }
}
