package com.example.rocacotizacion.ui.QuantityProd

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.DTO.SharedDataModel
import com.example.rocacotizacion.DataModel.DetalleItem
import com.example.rocacotizacion.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuantityProdFragment : Fragment() {
    private lateinit var editTextNumber: EditText
    private lateinit var tvPrice: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvnombreproducto: TextView
    private lateinit var tvcodigoproducto: TextView
    private lateinit var tvimpuesto: TextView
    private lateinit var iconDiscount: ImageView // Referencia al ImageView de descuento

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
        iconDiscount = view.findViewById(R.id.iconDiscount) // Inicializar el ImageView de descuento

        // Evento para ver descuentos a través del ícono de descuento
        iconDiscount.setOnClickListener {
            val codigoproducto = tvcodigoproducto.text.toString()
            if (codigoproducto.isNotEmpty() && tipoPago.isNotEmpty()) {
                val discountsDialog = DiscountsDialogFragment(codigoproducto, tipoPago)
                discountsDialog.show(parentFragmentManager, "DiscountsDialog")
            } else {
                Toast.makeText(context, "Información del producto incompleta", Toast.LENGTH_SHORT).show()
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

        if (idproducto != null && tipoPago.isNotEmpty()) {
            loadProductDetails(idproducto.toInt(), tipoPago)
        }

        return view
    }

    private fun agregarProducto() {
        val quantity = editTextNumber.text.toString().toIntOrNull() ?: 0
        val price = tvPrice.text.toString().removePrefix("L. ").toDoubleOrNull() ?: 0.0
        var subtotal = tvSubtotal.text.toString().removePrefix("L. ").toDoubleOrNull() ?: 0.0
        val impuesto = tvimpuesto.text.toString().toDoubleOrNull() ?: 0.0
        val nombreproducto = tvnombreproducto.text.toString()
        val codigoproducto = tvcodigoproducto.text.toString()
        var valorimpuesto = subtotal * (impuesto / 100)
        var total = subtotal + valorimpuesto

        CoroutineScope(Dispatchers.IO).launch {
            val firstItem = SharedDataModel.detalleItems.value?.firstOrNull()
            var porcentajeTotal = 0.0
            var descuento = 0.0

            firstItem?.let {
                val escalaDiscount = DatabaseApplication.getDatabase(requireContext())
                    .invdescuentoporescalaDAO().getDescuentoPorEscala(codigoproducto)
                    .firstOrNull { discount ->
                        quantity in discount.rangoinicial..discount.rangofinal
                    }

                porcentajeTotal += escalaDiscount?.monto ?: 0.0

                descuento = subtotal * (porcentajeTotal / 100)
                subtotal -= descuento
                valorimpuesto = subtotal * (impuesto / 100)
                total = subtotal + valorimpuesto
            }

            val detalleItem = DetalleItem(
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

            withContext(Dispatchers.Main) {
                SharedDataModel.detalleItems.value?.let { items ->
                    val updatedItems = ArrayList(items)
                    updatedItems.add(detalleItem)
                    SharedDataModel.detalleItems.postValue(updatedItems)
                }
                requireActivity().onBackPressed()
            }
        }
    }

    private fun calculateSubtotal() {
        val quantity = editTextNumber.text.toString().toIntOrNull() ?: 0
        val price = tvPrice.text.toString().removePrefix("L. ").toDoubleOrNull() ?: 0.0
        val subtotal = quantity * price
        tvSubtotal.text = "L. ${String.format("%.2f", subtotal)}"
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

    private fun loadProductDetails(idproducto: Int, codigotipoventa: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = DatabaseApplication.getDatabase(requireContext())
                val productDetails = db.ProductosDAO().getProductoConPrecio(idproducto, codigotipoventa)
                withContext(Dispatchers.Main) {
                    tvnombreproducto.text = productDetails.producto
                    tvcodigoproducto.text = productDetails.codigoproducto
                    tvPrice.text = "L. ${productDetails.precio}"
                    tvimpuesto.text = productDetails.porcentajeimpuesto.toString()
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
