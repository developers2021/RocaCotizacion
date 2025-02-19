package com.example.rocacotizacion.ui.configuracion

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.rocacotizacion.Adapter.ConditionHandler
import com.example.rocacotizacion.R
import com.example.rocacotizacion.ui.home.HomeFragment
import com.google.android.material.navigation.NavigationView

class ConfiguracionFragment : Fragment() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ConfiguracionPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla el layout para Configuración (asegúrate de que los IDs coincidan)
        return inflater.inflate(R.layout.fragment_configuracion, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Configura la Toolbar y el DrawerLayout
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_configuracion)
        drawerLayout = view.findViewById(R.id.drawer_layout_configuracion)

        // 2. Configura el ActionBarDrawerToggle para el ícono de "hamburger"
        toggle = ActionBarDrawerToggle(
            requireActivity(),
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 3. Configura el NavigationView (del Drawer) y su listener
        val navigationView = view.findViewById<NavigationView>(R.id.nav_configuracion_drawer)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    findNavController().navigate(R.id.nav_home)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_clientes -> {
                    findNavController().navigate(R.id.nav_clientes)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_configuracion -> {
                    // Ya estamos en Configuración; solo cierra el Drawer
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_gallery -> {
                    findNavController().navigate(R.id.nav_midia)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_slideshow -> {
                    ConditionHandler.showConfirmationDialog(requireContext())
                    true
                }
                else -> false
            }
        }

        // 4. Configurar el nombre de usuario en el Navigation Drawer
        val sharedPreferences = activity?.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val loggedInUsername = sharedPreferences?.getString("LoggedInUsername", null)

        loggedInUsername?.let { username ->
            // Obtener el agente desde la HomeFragment (o donde esté la lógica)
            HomeFragment.GetAgenteAsyncTask(requireContext(), username) { agente ->
                if (agente != null) {
                    // Asignar la información recuperada en los TextView del Drawer
                    navigationView.findViewById<TextView>(R.id.MenuName).text = agente.descripcionLarga
                    navigationView.findViewById<TextView>(R.id.textView).text = agente.descripcionCorta
                }
            }.execute()
        }

        // 5. Configura el ViewPager2 para mostrar la pantalla de sincronización parcial
        viewPager = view.findViewById(R.id.viewPagerConfiguracion)
        adapter = ConfiguracionPagerAdapter(this)
        viewPager.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        // Asegura que el Drawer siempre esté desbloqueado al volver al fragmento
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        toggle.syncState()
    }
}
