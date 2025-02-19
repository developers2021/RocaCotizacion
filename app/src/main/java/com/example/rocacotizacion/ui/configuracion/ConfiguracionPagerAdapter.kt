package com.example.rocacotizacion.ui.configuracion

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.rocacotizacion.PartialSyncFragment

class ConfiguracionPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 1 // Actualmente solo hay una pestaña

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PartialSyncFragment() // Fragment que contiene la sincronización parcial
            else -> throw IllegalArgumentException("Posición inválida: $position")
        }
    }
}
