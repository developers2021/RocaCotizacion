package com.example.rocacotizacion.ui.Clientes

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.rocacotizacion.R
import com.example.rocacotizacion.ui.home.HomeFragment
import com.google.android.material.navigation.NavigationView

// ClientesFragment.kt
class ClientesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_clientes, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Set up the ActionBarDrawerToggle
        val drawerLayout: DrawerLayout = view.findViewById(R.id.drawer_layout_clientes)
        val toolbar: Toolbar = view.findViewById(R.id.toolbar_clientes)
        val toggle = ActionBarDrawerToggle(
            activity,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        val sharedPreferences = activity?.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val loggedInUsername = sharedPreferences?.getString("LoggedInUsername", null)
        val navigationView: NavigationView = view.findViewById(R.id.nav_clientes)
        loggedInUsername?.let { username ->
            HomeFragment.GetAgenteAsyncTask(requireContext(), username) { agente ->
                // This is your callback that gets executed on the main thread.
                // Update your UI here with the agent details.
                if (agente != null) {
                    navigationView.findViewById<TextView>(R.id.MenuName).text = "${agente.descripcionLarga}"
                    navigationView.findViewById<TextView>(R.id.textView).text = "${agente.descripcionCorta}"
                }
            }.execute()
        }
    }
}
