package com.example.rocacotizacion.ui.ListaProducto

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.DTO.ProductoConPrecio
import com.example.rocacotizacion.R
import com.example.rocacotizacion.ui.QuantityProd.QuantityProdActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.appcompat.widget.SearchView

class ListaProductoFragment :Fragment() {
    private lateinit var productosAdapter: ProductosAdapter
    val codigoTipoVenta :String?=""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_listaproductos, container, false)
        //val clienteNombre = activity?.intent?.getStringExtra("clienteNombre")
        val codigoTipoVenta = activity?.intent?.getStringExtra("tipoPago") // Set this to the desired codigotipoventa value
        val searchViewClientes: SearchView = view.findViewById(R.id.searchViewProductos)

        // Initialize the adapter with an empty list
        productosAdapter = ProductosAdapter(emptyList(), { producto ->
            // Handle the click event here
            Toast.makeText(context, "Clicked on: ${producto.producto}", Toast.LENGTH_SHORT).show()
        }, codigoTipoVenta)
        // Set up the RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewlistaproductos)
         recyclerView.layoutManager = LinearLayoutManager(context)
         recyclerView.adapter = productosAdapter
        // Retrieve the arguments from the Bundle
        searchViewClientes.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                productosAdapter.filter(newText ?: "")
                return true
            }
        })
       codigoTipoVenta?.let { nonNullCodigoTipoVenta ->
           CoroutineScope(Dispatchers.IO).launch {
               val db = DatabaseApplication.getDatabase(requireContext())
               val productosConPrecio = db.ProductosDAO().getProductosConPrecio(nonNullCodigoTipoVenta)
//
               withContext(Dispatchers.Main) {
                   productosAdapter.updateProductos(productosConPrecio)
               }
           }
       } ?: Toast.makeText(context, "Tipo de Pago no está disponible", Toast.LENGTH_SHORT).show()

        return view
    }

    class ProductosAdapter(
        private var productos: List<ProductoConPrecio>,
        private val onProductoClickListener: (ProductoConPrecio) -> Unit,
        private val tipoPago: String?
    ) : RecyclerView.Adapter<ProductosAdapter.ViewHolder>() {

        private var productosFiltered = productos

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvProductoNombre: TextView = view.findViewById(R.id.tvProductoNombre)
            val tvProductoPrecio: TextView = view.findViewById(R.id.tvProductoPrecio)
            var idproducto: Int = 0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.producto_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val producto = productosFiltered[position]
            holder.tvProductoNombre.text = producto.producto
            holder.tvProductoPrecio.text = "L. ${producto.precio}"
            holder.idproducto = producto.idproducto
            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, QuantityProdActivity::class.java)
                intent.putExtra("idproducto", producto.idproducto.toString())
                intent.putExtra("tipoPago", tipoPago)
                context.startActivity(intent)
            }
        }

        override fun getItemCount(): Int = productosFiltered.size

        fun updateProductos(newProductos: List<ProductoConPrecio>) {
            productos = newProductos
            productosFiltered = newProductos
            notifyDataSetChanged()
        }

        fun filter(query: String) {
            productosFiltered = if (query.isEmpty()) {
                productos
            } else {
                productos.filter { it.producto.contains(query, ignoreCase = true) }
            }
            notifyDataSetChanged()
        }
    }

}