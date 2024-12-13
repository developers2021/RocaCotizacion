package com.example.rocacotizacion.ui.MiDia

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MiDiaPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    // Cachea las instancias de los fragmentos
    private val fragmentCache = mutableMapOf<Int, Fragment>()

    override fun getItemCount(): Int = 2 // Dos pestañas: Pedidos y Reporte de Cierre

    override fun createFragment(position: Int): Fragment {
        return fragmentCache.getOrPut(position) {
            when (position) {
                0 -> MiDiaPedidosFragment()
                1 -> CierreDiaFragment()
                else -> throw IllegalStateException("Posición inválida: $position")
            }
        }
    }
}


