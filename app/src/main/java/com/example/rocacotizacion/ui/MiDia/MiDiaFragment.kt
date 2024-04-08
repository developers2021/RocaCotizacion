package com.example.rocacotizacion.ui.MiDia

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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.DataModel.PedidoSummary
import com.example.rocacotizacion.R
import com.example.rocacotizacion.ui.home.HomeFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MiDiaFragment:Fragment() {
    private lateinit var viewModel: PedidoViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_midia, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Set up the ActionBarDrawerToggle
        super.onViewCreated(view, savedInstanceState)
        // Initialize the ViewModel
        viewModel = ViewModelProvider(this).get(PedidoViewModel::class.java)

        // Set up the RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewPedidos)
        val adapter = PedidoSummaryAdapter(listOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Observe changes in the pedidoHdrList and update the RecyclerView
        viewModel.pedidoHdrList.observe(viewLifecycleOwner) { pedidoHdrList ->
            val pedidoSummaryList = mutableListOf<PedidoSummary>()
            pedidoHdrList.forEach { hdr ->
                viewModel.getDetalleTotal(hdr.id).observe(viewLifecycleOwner) { detalleTotal ->
                    val pedidoSummary = PedidoSummary(hdr.id, hdr.tipopago, hdr.total)
                    pedidoSummaryList.add(pedidoSummary)
                    adapter.updateItems(pedidoSummaryList)
                }
            }
        }
        val drawerLayout: DrawerLayout = view.findViewById(R.id.drawer_layout_midia)
        val toolbar: Toolbar = view.findViewById(R.id.toolbar_midia)
        val toggle = ActionBarDrawerToggle(
            activity,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        //Set up of the username and description  in the title for the ActionBarDrawer
        val sharedPreferences = activity?.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val loggedInUsername = sharedPreferences?.getString("LoggedInUsername", null)
        val navigationView: NavigationView = view.findViewById(R.id.nav_midia)
        // Setting up the NavigationItemSelectedListener
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // Handle menu item selected
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
                R.id.nav_gallery -> {
                    findNavController().navigate(R.id.nav_midia)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                // Add more menu item clicks here
                else -> false
            }
        }

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