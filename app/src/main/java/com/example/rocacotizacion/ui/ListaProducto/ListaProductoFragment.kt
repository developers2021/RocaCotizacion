package com.example.rocacotizacion.ui.ListaProducto

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListaProductoFragment :Fragment() {
    private lateinit var productosAdapter: ProductosAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_listaproductos, container, false)
        // Retrieve the arguments from the Bundle
        val tipoPago = activity?.intent?.getStringExtra("tipoPago")
        //val clienteNombre = activity?.intent?.getStringExtra("clienteNombre")


        // Initialize the adapter with an empty list
         productosAdapter = ProductosAdapter(emptyList())

        // Set up the RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewlistaproductos)
         recyclerView.layoutManager = LinearLayoutManager(context)
         recyclerView.adapter = productosAdapter

        val codigoTipoVenta = tipoPago // Set this to the desired codigotipoventa value

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

     class ProductosAdapter(private var productos: List<ProductoConPrecio>) :
         RecyclerView.Adapter<ProductosAdapter.ViewHolder>() {

         class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
             val tvProductoNombre: TextView = view.findViewById(R.id.tvProductoNombre)
             val tvProductoPrecio: TextView = view.findViewById(R.id.tvProductoPrecio)
         }

         override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
             val view = LayoutInflater.from(parent.context).inflate(R.layout.producto_item, parent, false)
             return ViewHolder(view)
         }

         override fun onBindViewHolder(holder: ViewHolder, position: Int) {
             val producto = productos[position]
             holder.tvProductoNombre.text = producto.producto
             holder.tvProductoPrecio.text = "L. ${producto.precio}"
         }

         override fun getItemCount(): Int = productos.size

         fun updateProductos(newProductos: List<ProductoConPrecio>) {
             productos = newProductos
             notifyDataSetChanged()
         }
     }
}