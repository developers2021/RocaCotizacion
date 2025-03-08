package com.example.rocacotizacion.ui.configuracion

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.rocacotizacion.PartialSyncFragment
import com.example.rocacotizacion.ui.MiDia.MiDiaPedidosEditarFragment
import com.example.rocacotizacion.ui.MiDia.MiDiaPedidosFragment

class ConfiguracionPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    // Ahora tenemos 2 pestañas/páginas
    override fun getItemCount(): Int = 1

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PartialSyncFragment()    // Pestaña 1: Sincronización
            else -> throw IllegalArgumentException("Posición inválida: $position")
        }
    }
}
