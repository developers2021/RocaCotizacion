package com.example.rocacotizacion.ui.ListaProducto

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
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

        setStatusBarColorToGrayDark()

        // Obtener el tipo de venta desde el Intent
        codigoTipoVenta = activity?.intent?.getStringExtra("tipoPago")

        val searchViewProductos: SearchView = view.findViewById(R.id.searchViewProductos)

        // Expandir automáticamente el SearchView al hacer clic en cualquier parte
        searchViewProductos.setIconifiedByDefault(false)
        searchViewProductos.isFocusable = true
        searchViewProductos.requestFocusFromTouch()

        // Inicializar el adaptador con una lista vacía
        productosAdapter = ProductosAdapter(emptyList()) { producto, hasDiscount ->
            // Manejar el clic en el producto
            val intent = Intent(context, QuantityProdActivity::class.java).apply {
                putExtra("idproducto", producto.idproducto.toString())
                putExtra("codigoproducto", producto.codigoproducto)
                putExtra("nombreproducto", producto.producto)
                putExtra("precio", producto.precio?.toString() ?: "0.00")
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
                // No manejar la acción de submit
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filtrar la lista de productos en tiempo real
                productosAdapter.filter(newText ?: "")
                return true
            }
        })

        // Cargar los productos y sus estados de descuento
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = DatabaseApplication.getDatabase(requireContext())
                // Obtener todos los productos sin filtro
                val productosConPrecio = db.ProductosDAO().getProductosConPrecioSinFiltro()

                // Obtener los códigos de productos para buscar descuentos
                val codigosProductos = productosConPrecio.mapNotNull { it.codigoproducto }.distinct()
                val descuentosEscala = db.invdescuentoporescalaDAO().getDescuentoPorEscalaMultiple(codigosProductos)
                val descuentosTipoPago = db.invdescuentoportipoventaDAO().getDescuentosPorTipoVentaMultiple(codigosProductos, "")
                val descuentosRuta = db.invdescuentoporrutaDAO().getDescuentoPorRutaMultiple(codigosProductos)

                // Crear una lista de productos con descuento
                val productosConDescuento = descuentosEscala.map { it.codigoproducto } +
                        descuentosTipoPago.map { it.codigoproducto } +
                        descuentosRuta.map { it.codigoproducto }

                val productosConDescuentoSet = productosConDescuento.toSet()

                // Asignar estado de descuento a cada producto
                val productosConEstado = productosConPrecio.map { producto ->
                    val hasDiscount = producto.codigoproducto?.let { productosConDescuentoSet.contains(it) } ?: false
                    ProductoConDescuento(producto, hasDiscount)
                }

                // Ordenar alfabéticamente por nombre de producto
                val productosOrdenados = productosConEstado.sortedBy { it.producto.producto ?: "" }

                withContext(Dispatchers.Main) {
                    productosAdapter.updateProductos(productosOrdenados)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al cargar los productos", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }

     ?: run {
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

    fun Fragment.setStatusBarColorToGrayDark() {
        activity?.let { activity ->
            val window = activity.window
            val grayDarkColor = ContextCompat.getColor(activity, R.color.grayDark) // Asegúrate de que este color esté definido en colors.xml

            // Cambiar el color de la barra de notificaciones
            window.statusBarColor = grayDarkColor

            // Configurar el texto y los iconos en la barra de notificaciones (opcional)
            val decorView = window.decorView
            val controller = decorView.windowInsetsController
            controller?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        }
    }


}
