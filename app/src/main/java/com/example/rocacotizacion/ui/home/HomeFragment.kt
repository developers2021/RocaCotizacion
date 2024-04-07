package com.example.rocacotizacion.ui.home

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.rocacotizacion.DAO.Agente
import com.example.rocacotizacion.DAO.AppDatabase
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.R
import com.google.android.material.navigation.NavigationView

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Assuming you are storing the logged-in username in SharedPreferences
        val sharedPreferences = activity?.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val loggedInUsername = sharedPreferences?.getString("LoggedInUsername", null)
        // Set up the ActionBarDrawerToggle
        val drawerLayout: DrawerLayout = view.findViewById(R.id.drawer_layout_home)
        val toolbar: Toolbar = view.findViewById(R.id.toolbar_home)
        val toggle = ActionBarDrawerToggle(
            activity,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        // Find the NavigationView
        val navigationView: NavigationView = view.findViewById(R.id.nav_view_home)
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
        // Check if the header is already present before inflating

        loggedInUsername?.let { username ->
            GetAgenteAsyncTask(requireContext(), username) { agente ->
                // This is your callback that gets executed on the main thread.
                // Update your UI here with the agent details.
                if (agente != null) {
                    view.findViewById<TextView>(R.id.tvCodigoAgentes).text="${agente.descripcionCorta}"
                    view.findViewById<TextView>(R.id.textViewUsuario).text="${agente.descripcionLarga} - ${agente.username}"
                    view.findViewById<TextView>(R.id.textviewTipoAgente).text="${agente.tipoagente}"
                    view.findViewById<TextView>(R.id.textViewPOS).text = if (agente.flagPos == true) "Si" else "No"
                    view.findViewById<TextView>(R.id.textviewSucursal).text = "${agente.nombodega}"
                    navigationView.findViewById<TextView>(R.id.MenuName).text="${agente.descripcionLarga}"
                    navigationView.findViewById<TextView>(R.id.textView).text="${agente.descripcionCorta}"

                }
            }.execute()
        }
    }

    class GetAgenteAsyncTask(
        private val context: Context,
        private val username: String,
        private val callback: (Agente?) -> Unit
    ) : AsyncTask<Void, Void, Agente?>() {

        private val db: AppDatabase = DatabaseApplication.getDatabase(context)

        override fun doInBackground(vararg params: Void?): Agente? {
            // Perform database operation in background
            return db.AgenteDAO().getAgenteByUsername(username)
        }

        override fun onPostExecute(result: Agente?) {
            super.onPostExecute(result)
            // Runs on the main thread, update your UI here
            callback(result)
        }
    }
}
