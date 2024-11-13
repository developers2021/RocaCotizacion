package com.example.rocacotizacion.ui.QuantityProd

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiscountsDialogFragment(
    private val codigoproducto: String,
    private val codigotipoventa: String
) : DialogFragment() {

    private lateinit var recyclerViewDescuentos: RecyclerView
    private lateinit var btnCerrarDescuentos: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.dialog_discounts, container, false)

        // Inicializar RecyclerView y botón de cerrar
        recyclerViewDescuentos = view.findViewById(R.id.recyclerViewDescuentos)
        btnCerrarDescuentos = view.findViewById(R.id.btnCerrarDescuentos)

        // Configurar RecyclerView
        recyclerViewDescuentos.layoutManager = LinearLayoutManager(context)

        // Cargar descuentos desde la base de datos
        loadDiscounts()

        // Configurar botón de cerrar para cerrar el diálogo
        btnCerrarDescuentos.setOnClickListener {
            dismiss()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setGravity(Gravity.CENTER)
    }

    private fun loadDiscounts() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = DatabaseApplication.getDatabase(requireContext())
                val descuentosEscala = db.invdescuentoporescalaDAO().getDescuentoPorEscalaMultiple(listOf(codigoproducto))
                val descuentosTipoPago = db.invdescuentoportipoventaDAO().getDescuentosPorTipoVentaMultiple(listOf(codigoproducto), codigotipoventa)
                val descuentoRuta = db.invdescuentoporrutaDAO().getDescuentoPorRutafirst()

                // Crear lista de descuentos para el adaptador
                val descuentosList = mutableListOf<DiscountItem>()
                descuentosEscala.forEach { descuentosList.add(DiscountItem("Escala", it.monto)) }
                descuentosTipoPago.forEach { descuentosList.add(DiscountItem("Tipo de Pago", it.monto)) }
                descuentoRuta?.let { descuentosList.add(DiscountItem("Ruta", it.monto)) }

                withContext(Dispatchers.Main) {
                    if (descuentosList.isNotEmpty()) {
                        recyclerViewDescuentos.adapter = DiscountsAdapter(descuentosList)
                    } else {
                        Toast.makeText(context, "No hay descuentos disponibles", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al cargar descuentos", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
