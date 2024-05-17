package com.example.rocacotizacion.ui.MiDia

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.DAO.PedidoHdr
import com.example.rocacotizacion.DataModel.PedidoDtlS
import com.example.rocacotizacion.DataModel.PedidoHdrS
import com.example.rocacotizacion.DataModel.PedidoSummary
import com.example.rocacotizacion.DataModel.SendPedido
import com.example.rocacotizacion.R
import com.example.rocacotizacion.Utilidades
import com.example.rocacotizacion.ui.home.HomeFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.Request
import java.io.IOException


import org.json.JSONArray
import org.json.JSONObject
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
                PedidoSummary(hdr.id, hdr.tipopago, hdr.total,hdr.sinc,hdr.clientecodigo)
            }
            adapter.updateItems(pedidoSummaryList)
        }

        // Additional UI setup such as Drawer and NavigationView
        setupDrawer(view)

        //evento click para subir info
        val subirbtn=view.findViewById<Button>(R.id.btnsubirinfo)
        subirbtn.setOnClickListener {
            AlertDialog.Builder(context).apply {
                setTitle("Alerta")
                setMessage("¿Está seguro que desea sincronizar los pedidos?")
                setPositiveButton("Sincronizar") { dialog, _ ->
                    // Perform database check in the coroutine after user confirms
                    CoroutineScope(Dispatchers.IO).launch {
                        checkAndSyncPedidos()
                    }
                    dialog.dismiss()
                }
                setNeutralButton("Cancelar") { dialog, _ ->
                    Toast.makeText(context, "Sincronización cancelada", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                show()
            }
        }

    }
    private suspend fun checkAndSyncPedidos() {
        val db = context?.let { DatabaseApplication.getDatabase(it) }
        db?.let { database ->
            val headers = database.PedidoHdrDAO().getAllPedidoHdrS() // Make sure this operation is safe to call from a coroutine
            if (headers.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "No hay nada que sincronizar", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Move database access to Dispatchers.IO
                val agente = withContext(Dispatchers.IO) {
                    database.AgenteDAO().getAgente()
                }
                if (agente != null) {
                    val pedidos = headers.map { header ->
                        val details = withContext(Dispatchers.IO) {
                            database.PedidoDtlDAO().getAllDetailsByHeaderId(header.id)
                        }
                        PedidoHdrS(
                            id = header.id,
                            tipoPago = header.tipopago,
                            subtotal = header.subtotal,
                            descuento = header.descuento,
                            total = header.total,
                            clientecodigo = header.clientecodigo,
                            isSincronizado = header.sinc,
                            pedidoDtls = details.map { detail ->
                                PedidoDtlS(
                                    id = detail.id,
                                    idhdr = detail.idhdr,
                                    codigoProducto = detail.codigoproducto,
                                    cantidad = detail.cantidad,
                                    precio = detail.precio,
                                    descuento = detail.descuento
                                )
                            }
                        )
                    }
                    withContext(Dispatchers.Main) {
                        val sendPedido = SendPedido(
                            idAgentes = agente.idAgentes,
                            codigoAgentes = agente.codigoAgentes,
                            idSucursal = agente.idSucursal,
                            idBodega = agente.idbodega,
                            pedidos = pedidos
                        )
                        val jsonresult=createJson(sendPedido)
                        Log.d("Obj sendPedido", "sendPedido: $jsonresult")
                        sendPedido(jsonresult) {
                            // Code to execute after sendPedido completes
                            println("Pedido sending completed")
                        }
                        // Synchronize or perform additional actions with sendPedido
                        Toast.makeText(context, "Sincronización de pedidos para agente: ${agente.username}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No hay datos de agente disponibles.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } ?: withContext(Dispatchers.Main) {
            Toast.makeText(context, "Database access error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createJson(sendPedido: SendPedido): String {
        val root = JSONObject()
        root.put("idAgentes", sendPedido.idAgentes)
        root.put("codigoAgentes", sendPedido.codigoAgentes)
        root.put("idSucursal", sendPedido.idSucursal)
        root.put("idBodega", sendPedido.idBodega)

        val pedidosArray = JSONArray()
        sendPedido.pedidos.forEach { pedidoHdr ->
            val pedidoHdrJson = JSONObject()
            pedidoHdrJson.put("id", pedidoHdr.id)
            pedidoHdrJson.put("tipoPago", pedidoHdr.tipoPago)
            pedidoHdrJson.put("subtotal", pedidoHdr.subtotal)
            pedidoHdrJson.put("descuento", pedidoHdr.descuento)
            pedidoHdrJson.put("total", pedidoHdr.total)
            pedidoHdrJson.put("clientecodigo", pedidoHdr.clientecodigo)
            pedidoHdrJson.put("isSincronizado", pedidoHdr.isSincronizado)

            val pedidoDtlsArray = JSONArray()
            pedidoHdr.pedidoDtls.forEach { pedidoDtl ->
                val pedidoDtlJson = JSONObject()
                pedidoDtlJson.put("id", pedidoDtl.id)
                pedidoDtlJson.put("idhdr", pedidoDtl.idhdr)
                pedidoDtlJson.put("codigoProducto", pedidoDtl.codigoProducto)
                pedidoDtlJson.put("cantidad", pedidoDtl.cantidad)
                pedidoDtlJson.put("precio", pedidoDtl.precio)
                pedidoDtlJson.put("descuento", pedidoDtl.descuento)
                pedidoDtlsArray.put(pedidoDtlJson)
            }

            pedidoHdrJson.put("pedidoDtls", pedidoDtlsArray)
            pedidosArray.put(pedidoHdrJson)
        }

        root.put("pedidos", pedidosArray)

        return root.toString(4) // Pretty print with indentation
    }

    private fun sendPedido(jsonToSend: String, onComplete: () -> Unit) {
        val ids = extractIdsFromJson(jsonToSend) // Extract IDs before sending

        if (isOnline(requireContext())) {
            val okHttpClient = OkHttpClient()
            val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonToSend)

            val request = Request.Builder()
                .url(Utilidades.URL_SAVE_JSON_PEDIDO)
                .post(requestBody)
                .build()

            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Failed to send data: ${e.message}", Toast.LENGTH_SHORT).show()
                        onComplete()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (it.isSuccessful) {
                            val responseBodyString = it.body?.string()

                            // Parsing the JSON response
                            val jsonResponse = JSONObject(responseBodyString)
                            val resultObject = jsonResponse.getJSONObject("result")  // Get the 'result' object
                            val success = resultObject.getBoolean("result")  // Get boolean from within the 'result' object
                            val message = resultObject.getString("message")

                            activity?.runOnUiThread {
                                if (success) {
                                    updateSincStatus(ids) // Update the database if the synchronization is successful
                                    Toast.makeText(context, "Data successfully synchronized", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                                onComplete()
                            }
                        } else {
                            activity?.runOnUiThread {
                                Toast.makeText(context, "Server error: ${response.message}", Toast.LENGTH_SHORT).show()
                                onComplete()
                            }
                        }
                    }
                }
            })
        } else {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
            onComplete()
        }
    }

    private fun extractIdsFromJson(jsonString: String): List<Int> {
        val ids = mutableListOf<Int>()
        val jsonObject = JSONObject(jsonString)
        val pedidosArray = jsonObject.getJSONArray("pedidos")
        for (i in 0 until pedidosArray.length()) {
            val pedido = pedidosArray.getJSONObject(i)
            val id = pedido.getInt("id")
            ids.add(id)
        }
        return ids
    }

    private fun updateSincStatus(ids: List<Int>) {
        CoroutineScope(Dispatchers.IO).launch {
            context?.let {
                val db = DatabaseApplication.getDatabase(it)
                db.PedidoHdrDAO().updateSincForIds(ids, true)
            }
        }
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
    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }

}
