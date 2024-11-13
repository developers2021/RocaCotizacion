package com.example.rocacotizacion.ui.ListaProducto

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import com.example.rocacotizacion.DAO.*
import com.example.rocacotizacion.DTO.ProductoConPrecio
import com.example.rocacotizacion.R
import com.example.rocacotizacion.ui.QuantityProd.QuantityProdActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListaProductoFragment : Fragment() {

    private lateinit var productosAdapter: ProductosAdapter
    private var codigoTipoVenta: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_listaproductos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener el tipo de venta desde el Intent
        codigoTipoVenta = activity?.intent?.getStringExtra("tipoPago")

        val searchViewProductos: SearchView = view.findViewById(R.id.searchViewProductos)

        // Inicializar el adaptador con una lista vacía
        productosAdapter = ProductosAdapter(emptyList()) { producto, hasDiscount ->
            // Manejar el evento de clic en el producto
            Toast.makeText(context, "Clicked on: ${producto.producto}", Toast.LENGTH_SHORT).show()
            // Iniciar la actividad de agregar producto con detalles
            val intent = Intent(context, QuantityProdActivity::class.java).apply {
                putExtra("idproducto", producto.idproducto.toString())
                putExtra("tipoPago", codigoTipoVenta)
            }
            context?.startActivity(intent)
        }

        // Configurar el RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewlistaproductos)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = productosAdapter

        // Configurar el SearchView para filtrar productos
        searchViewProductos.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false // No manejar la acción de submit
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                productosAdapter.filter(newText ?: "")
                return true
            }
        })

        // Cargar los productos y sus estados de descuento
        codigoTipoVenta?.let { nonNullCodigoTipoVenta ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = DatabaseApplication.getDatabase(requireContext())
                    val productosConPrecio = db.ProductosDAO().getProductosConPrecio(nonNullCodigoTipoVenta)

                    // Obtener todos los códigos de productos para verificar descuentos
                    val codigosProductos = productosConPrecio.mapNotNull { it.codigoproducto }.distinct()

                    // Obtener descuentos por escala
                    val descuentosEscala = db.invdescuentoporescalaDAO().getDescuentoPorEscalaMultiple(codigosProductos)

                    // Obtener descuentos por tipo de pago
                    val descuentosTipoPago = db.invdescuentoportipoventaDAO().getDescuentosPorTipoVentaMultiple(codigosProductos, nonNullCodigoTipoVenta)

                    // Obtener descuentos por ruta
                    val descuentosRuta = db.invdescuentoporrutaDAO().getDescuentoPorRutaMultiple(codigosProductos)

                    // Crear un conjunto de códigos de productos que tienen al menos un descuento
                    val productosConDescuento = descuentosEscala.map { it.codigoproducto } +
                            descuentosTipoPago.map { it.codigoproducto } +
                            descuentosRuta.map { it.codigoproducto }

                    val productosConDescuentoSet = productosConDescuento.toSet()

                    // Crear una lista de productos con el estado de descuento
                    val productosConEstado = productosConPrecio.map { producto ->
                        val hasDiscount = producto.codigoproducto?.let { productosConDescuentoSet.contains(it) } ?: false
                        ProductoConDescuento(producto, hasDiscount)
                    }

                    withContext(Dispatchers.Main) {
                        productosAdapter.updateProductos(productosConEstado)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error al cargar los productos", Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                }
            }
        } ?: run {
            Toast.makeText(context, "Tipo de Pago no está disponible", Toast.LENGTH_SHORT).show()
        }
    }

    // Data class para asociar un producto con su estado de descuento
    data class ProductoConDescuento(
        val producto: ProductoConPrecio,
        val hasDiscount: Boolean
    )

    class ProductosAdapter(
        private var productos: List<ProductoConDescuento>,
        private val onProductoClickListener: (ProductoConPrecio, Boolean) -> Unit
    ) : RecyclerView.Adapter<ProductosAdapter.ViewHolder>() {

        private var productosFiltered = productos

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvProductoNombre: android.widget.TextView = view.findViewById(R.id.tvProductoNombre)
            val tvProductoPrecio: android.widget.TextView = view.findViewById(R.id.tvProductoPrecio)
            val tvProductoDescripcion: android.widget.TextView = view.findViewById(R.id.tvProductoDescripcion)
            val ivDescuento: android.widget.ImageView = view.findViewById(R.id.ivDescuento) // Nuevo ImageView para descuento
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.producto_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val productoConDescuento = productosFiltered[position]
            val producto = productoConDescuento.producto

            holder.tvProductoNombre.text = producto.producto ?: "Sin Nombre"
            holder.tvProductoPrecio.text = "L. ${producto.precio?.let { String.format("%.2f", it) } ?: "0.00"}"
            holder.tvProductoDescripcion.text = producto.codigoproducto ?: "Código N/A"

            // Manejar la visibilidad del ícono de descuento
            holder.ivDescuento.visibility = if (productoConDescuento.hasDiscount) View.VISIBLE else View.GONE

            // Manejar el clic en el ícono de descuento (Opcional)
            holder.ivDescuento.setOnClickListener {
                Toast.makeText(it.context, "Descuento activo para ${producto.producto}", Toast.LENGTH_SHORT).show()
                // Aquí puedes agregar lógica adicional, como mostrar detalles del descuento
            }

            // Manejar el clic en el ítem completo
            holder.itemView.setOnClickListener {
                onProductoClickListener(producto, productoConDescuento.hasDiscount)
            }
        }

        override fun getItemCount(): Int = productosFiltered.size

        fun updateProductos(newProductos: List<ProductoConDescuento>) {
            productos = newProductos
            productosFiltered = newProductos
            notifyDataSetChanged()
        }

        fun filter(query: String) {
            productosFiltered = if (query.isEmpty()) {
                productos
            } else {
                productos.filter {
                    it.producto.producto?.contains(query, ignoreCase = true) == true
                }
            }
            notifyDataSetChanged()
        }
    }

}
