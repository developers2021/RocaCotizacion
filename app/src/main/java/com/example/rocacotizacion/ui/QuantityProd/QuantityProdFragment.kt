import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quantityprod, container, false)

        val tipoPago = activity?.intent?.getStringExtra("tipoPago")
        val idproducto = activity?.intent?.getStringExtra("idproducto")

        editTextNumber = view.findViewById(R.id.editTextNumber)
        val buttonIncrement: Button = view.findViewById(R.id.buttonIncrement)
        val buttonDecrement: Button = view.findViewById(R.id.buttonDecrement)
        val btnAgregar: Button = view.findViewById(R.id.btnAgregar)
        tvPrice = view.findViewById(R.id.tvValueRight1)
        tvSubtotal = view.findViewById(R.id.tvValueRight3)
        tvnombreproducto = view.findViewById(R.id.tvLargeText)
        tvcodigoproducto = view.findViewById(R.id.tvSmallText)
        btnAgregar.setOnClickListener {
            val quantity = editTextNumber.text.toString().toIntOrNull() ?: 0
            val price = tvPrice.text.toString().removePrefix("L. ").toDoubleOrNull() ?: 0.0
            val subtotal = tvSubtotal.text.toString().removePrefix("L. ").toDoubleOrNull() ?: 0.0
            val nombreproducto = tvnombreproducto.text.toString()
            val codigoproducto = tvcodigoproducto.text.toString()
            val detalleItem = DetalleItem(quantity, price, subtotal, nombreproducto, codigoproducto)

            SharedDataModel.detalleItems.value?.let { items ->
                val updatedItems = ArrayList(items)
                updatedItems.add(detalleItem)
                SharedDataModel.detalleItems.postValue(updatedItems)
            }
            Log.d("QuantityProdFragment", "List updated: ${SharedDataModel.detalleItems.value}")
            requireActivity().onBackPressed()
        }
        buttonIncrement.setOnClickListener {
            incrementQuantity()
        }

        buttonDecrement.setOnClickListener {
            decrementQuantity()
        }

        editTextNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not used
            }

            override fun afterTextChanged(s: Editable?) {
                calculateSubtotal()
            }
        })

        if (idproducto != null && tipoPago != null) {
            loadProductDetails(idproducto.toInt(), tipoPago)
        }

        return view
    }

    private fun loadProductDetails(idproducto: Int, codigotipoventa: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = DatabaseApplication.getDatabase(requireContext())
                val productDetails = db.ProductosDAO().getProductoConPrecio(idproducto, codigotipoventa)
                withContext(Dispatchers.Main) {
                    view?.findViewById<TextView>(R.id.tvLargeText)?.text = productDetails.producto
                    view?.findViewById<TextView>(R.id.tvSmallText)?.text = "${productDetails.codigoproducto}"
                    tvPrice.text = "L. ${productDetails.precio.toString()}"
                    view?.findViewById<TextView>(R.id.tvValueRight2)?.text = "${productDetails.porcentajeimpuesto} %"
                    calculateSubtotal()  // Call to update the subtotal
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error loading product details", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun calculateSubtotal() {
        val quantity = editTextNumber.text.toString().toIntOrNull() ?: 0
        val priceString = tvPrice.text.toString().removePrefix("L. ").toDoubleOrNull() ?: 0.0
        val subtotal = quantity * priceString
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
}
