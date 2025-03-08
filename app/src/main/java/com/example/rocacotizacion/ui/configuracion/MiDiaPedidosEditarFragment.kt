package com.example.rocacotizacion.ui.MiDia

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.R
import com.example.rocacotizacion.DataModel.PedidoSummary
import com.example.rocacotizacion.ui.MiDia.PedidoSummaryAdapter
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import com.example.rocacotizacion.Adapter.ConditionHandler
import com.example.rocacotizacion.ui.Facturacion.FacturacionActivity
import com.example.rocacotizacion.ui.home.HomeFragment

class MiDiaPedidosEditarFragment : Fragment() {

    private lateinit var viewModel: PedidoViewModel
    private lateinit var adapter: PedidoSummaryAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Layout con DrawerLayout, Toolbar, NavigationView, etc.
        return inflater.inflate(R.layout.fragment_editar_pedidos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Configurar Toolbar
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_editar_pedidos)
        toolbar.setBackgroundColor(requireContext().getColor(R.color.white))
        toolbar.setTitleTextColor(requireContext().getColor(R.color.textPrimary))

        // 2) Configurar DrawerLayout
        val drawerLayout: DrawerLayout = view.findViewById(R.id.drawer_layout_editar_pedidos)
        val toggle = ActionBarDrawerToggle(
            requireActivity(),
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 3) Configurar NavigationView
        val navigationView: NavigationView = view.findViewById(R.id.nav_view_editar_pedidos)
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
                    findNavController().navigate(R.id.nav_midia)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_configuracion -> {
                    findNavController().navigate(R.id.nav_configuracion)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_editarpedido -> {
                    // Ya estás en "Editar Pedidos"; puedes recargar o simplemente cerrar
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

        // (Opcional) Configurar nombre de usuario en el header del Navigation Drawer
        val sharedPreferences = activity?.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val loggedInUsername = sharedPreferences?.getString("LoggedInUsername", null)
        loggedInUsername?.let { username ->
            val headerView = navigationView.getHeaderView(0)
            // Llamamos la misma AsyncTask que en HomeFragment para mostrar la ruta/nombre de vendedor
            HomeFragment.GetAgenteAsyncTask(requireContext(), username) { agente ->
                if (agente != null) {
                    headerView.findViewById<TextView>(R.id.MenuName)?.text = agente.descripcionLarga
                    headerView.findViewById<TextView>(R.id.textView)?.text = agente.descripcionCorta
                }
            }.execute()
        }

        // 4) Configurar RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewPedidos)
        adapter = PedidoSummaryAdapter(mutableListOf()) { pedidoSummary ->
            // Al hacer clic en un pedido, abrimos FacturacionActivity en modo editar
            navigateToFacturacionEditar(
                pedidoSummary.id,
                pedidoSummary.tipopago,
                pedidoSummary.Codigocliente
            )
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 5) Inicializar ViewModel y observar datos
        viewModel = ViewModelProvider(requireActivity()).get(PedidoViewModel::class.java)
        viewModel.pedidoHdrList.observe(viewLifecycleOwner) { pedidoHdrList ->
            // Filtrar para sólo pedidos NO sincronizados
            val unsyncedList = pedidoHdrList.filter { hdr -> hdr.sinc == false }
            val pedidoSummaryList = unsyncedList.map { hdr ->
                PedidoSummary(
                    hdr.id,
                    hdr.codigopedido,
                    hdr.tipopago,
                    hdr.total,
                    hdr.sinc,
                    hdr.clientecodigo,
                    hdr.anulado
                )
            }
            adapter.updateItems(pedidoSummaryList)
        }
    }

    /**
     * Abre FacturacionActivity en modo "editar" con los datos del pedido
     */
    private fun navigateToFacturacionEditar(
        pedidoId: Int,
        tipoPago: String,
        clientecodigo: String
    ) {
        try {
            val intent = Intent(requireContext(), FacturacionActivity::class.java).apply {
                putExtra("modo", "editar")
                putExtra("pedidoId", pedidoId)
                putExtra("tipoPago", tipoPago)          // EJ: "CTADO" or "CRED"
                putExtra("clientecodigo", clientecodigo)
                // putExtra("clienteNombre", "Si lo obtienes de otra fuente, agrégalo aquí")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("MiDiaPedidosEditarFragment", "Error al iniciar FacturacionActivity: ${e.message}")
            Toast.makeText(requireContext(), "Error al abrir edición de pedido", Toast.LENGTH_SHORT).show()
        }
    }
}
