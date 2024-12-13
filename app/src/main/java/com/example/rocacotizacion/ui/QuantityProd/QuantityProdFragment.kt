package com.example.rocacotizacion.ui.QuantityProd

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.DTO.SharedDataModel
import com.example.rocacotizacion.DataModel.DetalleItem
import com.example.rocacotizacion.R
import com.example.rocacotizacion.ui.Facturacion.FacturacionActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

class QuantityProdFragment : Fragment() {
    private lateinit var editTextNumber: EditText
    private lateinit var tvPrice: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvnombreproducto: TextView
    private lateinit var tvcodigoproducto: TextView
    private lateinit var tvimpuesto: TextView
    private lateinit var iconDiscount: ImageView

    private val decimalFormat = DecimalFormat("#,###.00")

    private fun formatNumber(number: Double): String {
        return if (number == 0.0) "0.00" else decimalFormat.format(number)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cambiar el color de la barra de estado y barra de navegación
        requireActivity().window.apply {
            statusBarColor = requireContext().getColor(R.color.white)
            navigationBarColor = requireContext().getColor(R.color.white)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quantityprod, container, false)

        val tipoPago = activity?.intent?.getStringExtra("tipoPago") ?: ""
        val idproducto = activity?.intent?.getStringExtra("idproducto")

        editTextNumber = view.findViewById(R.id.editTextNumber)
        val buttonIncrement: Button = view.findViewById(R.id.buttonIncrement2)
        val buttonDecrement: Button = view.findViewById(R.id.buttonDecrement2)
        val btnAgregar: Button = view.findViewById(R.id.btnAgregar)
        val btnCancelar: Button = view.findViewById(R.id.btnCancel)
        tvPrice = view.findViewById(R.id.tvValueRight1)
        tvSubtotal = view.findViewById(R.id.tvValueRight3)
        tvnombreproducto = view.findViewById(R.id.tvLargeText)
        tvcodigoproducto = view.findViewById(R.id.tvSmallText)
        tvimpuesto = view.findViewById(R.id.tvValueRight2)
        iconDiscount = view.findViewById(R.id.iconDiscount)

        iconDiscount.setOnClickListener {
            val codigoproducto = tvcodigoproducto.text.toString()
            val nombreproducto = tvnombreproducto.text.toString()
            val tipoPago = activity?.intent?.getStringExtra("tipoPago") // Recuperar tipoPago desde el Intent

            if (codigoproducto.isNotEmpty() && nombreproducto.isNotEmpty() && !tipoPago.isNullOrEmpty()) {
                val discountsDialog = DiscountsDialogFragment(codigoproducto, nombreproducto, tipoPago)
                discountsDialog.show(parentFragmentManager, "DiscountsDialog")
            } else {
                Toast.makeText(context, "Información del producto o tipo de pago incompleta", Toast.LENGTH_SHORT).show()
            }
        }









        btnAgregar.setOnClickListener {
            agregarProducto()
        }

        buttonIncrement.setOnClickListener {
            incrementQuantity()
        }

        buttonDecrement.setOnClickListener {
            decrementQuantity()
        }

        btnCancelar.setOnClickListener {
            requireActivity().onBackPressed()
        }

        editTextNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                calculateSubtotal()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        if (idproducto != null) {
            loadProductDetails(idproducto.toInt())
        }

        return view
    }

    private fun agregarProducto() {
        val quantity = editTextNumber.text.toString().toIntOrNull() ?: 0
        val price = tvPrice.text.toString().removePrefix("L. ").replace(",", "").toDoubleOrNull() ?: 0.0
        var subtotal = price * quantity
        val impuesto = tvimpuesto.text.toString().toDoubleOrNull() ?: 0.0
        val nombreproducto = tvnombreproducto.text.toString()
        val codigoproducto = tvcodigoproducto.text.toString()
        var valorimpuesto = subtotal * (impuesto / 100)
        var total = subtotal + valorimpuesto

        CoroutineScope(Dispatchers.IO).launch {
            var porcentajeTotal = 0.0
            var descuento = 0.0

            // Verificar si los descuentos están habilitados antes de aplicarlos
            if (SharedDataModel.checkedDescuentoEscala) {
                val escalaDiscount = DatabaseApplication.getDatabase(requireContext())
                    .invdescuentoporescalaDAO().getDescuentoPorEscala(codigoproducto)
                    .firstOrNull { discount ->
                        quantity in discount.rangoinicial..discount.rangofinal
                    }

                if (escalaDiscount != null) {
                    porcentajeTotal += escalaDiscount.monto
                    descuento = subtotal * (porcentajeTotal / 100)
                    subtotal -= descuento
                }
            }

            valorimpuesto = subtotal * (impuesto / 100)
            total = subtotal + valorimpuesto

            val newItem = DetalleItem(
                quantity = quantity,
                price = price,
                subtotal = subtotal,
                nombreproducto = nombreproducto,
                codigoproducto = codigoproducto,
                descuento = descuento,
                porcentajeTotal = porcentajeTotal,
                porcentajeImpuesto = impuesto,
                valorimpuesto = valorimpuesto,
                total = total
            )

            // Obtener la lista actual de ítems en el carrito
            val currentItems = SharedDataModel.detalleItems.value?.toMutableList() ?: mutableListOf()

            // Buscar si el producto ya existe en el carrito
            val existingItemIndex = currentItems.indexOfFirst { it.codigoproducto == newItem.codigoproducto }

            if (existingItemIndex != -1) {
                // El producto ya existe en el carrito, suma las cantidades y actualiza los valores
                val existingItem = currentItems[existingItemIndex]
                existingItem.quantity += newItem.quantity
                existingItem.subtotal += newItem.subtotal
                existingItem.descuento += newItem.descuento
                existingItem.valorimpuesto += newItem.valorimpuesto
                existingItem.total += newItem.total
            } else {
                // El producto no existe en el carrito, agrégalo a la lista
                currentItems.add(newItem)
            }

            withContext(Dispatchers.Main) {
                SharedDataModel.detalleItems.postValue(currentItems)
                requireActivity().onBackPressed()
            }
        }
    }

    private fun calculateSubtotal() {
        val quantity = editTextNumber.text.toString().toIntOrNull() ?: 0
        val price = tvPrice.text.toString().removePrefix("L. ").replace(",", "").toDoubleOrNull() ?: 0.0
        val subtotal = quantity * price
        tvSubtotal.text = "L. ${formatNumber(subtotal)}"
    }

    private fun incrementQuantity() {
        val currentQuantity = editTextNumber.text.toString().toIntOrNull() ?: 0
        editTextNumber.setText((currentQuantity + 1).toString())
        calculateSubtotal()
    }

    private fun decrementQuantity() {
        val currentQuantity = editTextNumber.text.toString().toIntOrNull() ?: 0
        if (currentQuantity > 0) {
            editTextNumber.setText((currentQuantity - 1).toString())
            calculateSubtotal()
        }
    }

    private fun loadProductDetails(idproducto: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = DatabaseApplication.getDatabase(requireContext())
                val productDetails = db.ProductosDAO().getProductoConPrecio(idproducto)
                withContext(Dispatchers.Main) {
                    tvnombreproducto.text = productDetails.producto
                    tvcodigoproducto.text = productDetails.codigoproducto
                    tvPrice.text = "L. ${formatNumber(productDetails.precio ?: 0.0)}"
                    tvimpuesto.text = formatNumber(productDetails.porcentajeimpuesto)
                    calculateSubtotal()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al cargar detalles del producto", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
