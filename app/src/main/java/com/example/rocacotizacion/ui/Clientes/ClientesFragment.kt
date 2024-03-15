package com.example.rocacotizacion.ui.Clientes

import android.app.AlertDialog
import android.content.Context
import android.os.AsyncTask
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
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DAO.Agente
import com.example.rocacotizacion.DAO.AppDatabase
import com.example.rocacotizacion.DAO.Clientes
import com.example.rocacotizacion.DAO.DatabaseApplication
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

        //Set up of the username and description  in the title for the ActionBarDrawer
        val sharedPreferences = activity?.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val loggedInUsername = sharedPreferences?.getString("LoggedInUsername", null)
        val navigationView: NavigationView = view.findViewById(R.id.nav_clientes)
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

            GetClienteAsyncTask(requireContext()) { clientesList ->
                val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewClientes)
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = ClientesAdapter(clientesList) { cliente ->
                    // The lambda here is where the AlertDialog will be shown when a row is tapped
                }
            }.execute()

        }
    }

    class GetClienteAsyncTask(
        private val context: Context,
        private val callback: (List<Clientes>) -> Unit
    ) : AsyncTask<Void, Void, List<Clientes>>() {
        private val db: AppDatabase = DatabaseApplication.getDatabase(context)
        override fun doInBackground(vararg params: Void?): List<Clientes> {
            // Perform database operation in background
            return db.ClientesDAO().getSelectClientes()
        }
        override fun onPostExecute(result: List<Clientes>) {
            super.onPostExecute(result)
            // Runs on the main thread, update your UI here
            callback(result)
        }
    }
    class ClientesAdapter(
        private val clientesList: List<Clientes>,
        private val onItemClick: (Clientes) -> Unit
    ) : RecyclerView.Adapter<ClientesAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(R.id.textViewCliente)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.cliente_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val cliente = clientesList[position]
            holder.textView.text = cliente.nombrecliente // Assuming 'nombre' is a property of the Clientes class
            holder.itemView.setOnClickListener {
                // Show an AlertDialog when a row is tapped
                val context = holder.itemView.context
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Tipo de Pago")
                builder.setMessage("Escoja el tipo de pago para la creacion del pedido")

                builder.setPositiveButton("Contado") { dialog, _ ->
                    // Handle "Yes" button click
                    Toast.makeText(context, "Pedido Contado para : ${cliente.nombrecliente}", Toast.LENGTH_SHORT).show()
                    val navController = Navigation.findNavController(holder.itemView)
                    navController.navigate(R.id.action_clientes_to_facturacion)

                    dialog.dismiss()
                }

                builder.setNegativeButton("Credito") { dialog, _ ->
                    // Handle "No" button click
                    Toast.makeText(context, "Pedido Credito para : ${cliente.nombrecliente}", Toast.LENGTH_SHORT).show()
                    val navController = Navigation.findNavController(holder.itemView)
                    navController.navigate(R.id.action_clientes_to_facturacion)
                    dialog.dismiss()
                }

                builder.setNeutralButton("Cancelar") { dialog, _ ->
                    // Handle "Cancel" button click
                    dialog.dismiss()
                }

                val alertDialog = builder.create()
                alertDialog.show()

            }
        }

        override fun getItemCount(): Int = clientesList.size

    }



}
