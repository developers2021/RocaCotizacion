package com.example.rocacotizacion.ui.MiDia

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DataModel.PedidoSummary
import com.example.rocacotizacion.R
import com.example.rocacotizacion.ui.Clientes.ClientesFragment
import com.example.rocacotizacion.ui.home.HomeFragment
import com.google.android.material.navigation.NavigationView

class MiDiaFragment : Fragment() {
    private lateinit var viewModel: PedidoViewModel
    private lateinit var adapter: PedidoSummaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_midia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the ViewModel
        viewModel = ViewModelProvider(this).get(PedidoViewModel::class.java)

        // Setup RecyclerView
        setupRecyclerView(view)

        // Observing data changes from ViewModel
        viewModel.pedidoHdrList.observe(viewLifecycleOwner) { pedidoHdrList ->
            val pedidoSummaryList = pedidoHdrList.map { hdr ->
                PedidoSummary(hdr.id, hdr.tipopago, hdr.total)
            }
            adapter.updateItems(pedidoSummaryList)
        }

        // Additional UI setup such as Drawer and NavigationView
        setupDrawer(view)

        // If using SharedPreferences or other initializations, add them here
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewPedidos)
        adapter = PedidoSummaryAdapter(mutableListOf()) { pedidoSummary ->
            val bundle = Bundle().apply {
                putInt("pedidoId", pedidoSummary.id)
            }
            findNavController().navigate(R.id.nav_detallePedido, bundle)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun setupDrawer(view: View) {
        val drawerLayout: DrawerLayout = view.findViewById(R.id.drawer_layout_midia)
        val toolbar: Toolbar = view.findViewById(R.id.toolbar_midia)
        val toggle = ActionBarDrawerToggle(
            activity, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        val sharedPreferences = activity?.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val loggedInUsername = sharedPreferences?.getString("LoggedInUsername", null)
        val navigationView: NavigationView = view.findViewById(R.id.nav_midia)
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
                R.id.nav_gallery -> {
                    findNavController().navigate(R.id.nav_gallery)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
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
