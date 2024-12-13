package com.example.rocacotizacion.ui.QuantityProd

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.DataModel.DiscountItem
import com.example.rocacotizacion.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiscountsDialogFragment(
    private val codigoproducto: String,
    private val nombreproducto: String,
    private val tipoPago: String // Tipo de pago seleccionado
) : DialogFragment() {

    private lateinit var recyclerViewDescuentos: RecyclerView
    private lateinit var btnCerrarDescuentos: Button
    private lateinit var tvProductName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.dialog_discounts, container, false)

        // Inicializar vistas
        tvProductName = view.findViewById(R.id.tvProductName)
        recyclerViewDescuentos = view.findViewById(R.id.recyclerViewDescuentos)
        btnCerrarDescuentos = view.findViewById(R.id.btnCerrarDescuentos)

        // Mostrar el nombre del producto
        tvProductName.text = nombreproducto

        // Configurar RecyclerView
        recyclerViewDescuentos.layoutManager = LinearLayoutManager(context)

        // Cargar descuentos desde la base de datos
        loadDiscounts()

        // Configurar botón de cerrar
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

                // Obtener descuentos por escala
                val descuentosEscala = db.invdescuentoporescalaDAO()
                    .getDescuentoPorEscalaMultiple(listOf(codigoproducto))
                    .sortedBy { it.rangoinicial }

                // Obtener descuentos por tipo de venta basados en el tipoPago
                val descuentosTipoVenta = db.invdescuentoportipoventaDAO()
                    .getDescuentosPorTipoVentaMultiple(listOf(codigoproducto), tipoPago)

                val descuentosList = mutableListOf<DiscountItem>()

                // Agrupar descuentos por escala
                if (descuentosEscala.isNotEmpty()) {
                    val rangosUnidades = descuentosEscala.joinToString(separator = "\n") {
                        "${it.rangoinicial} - ${it.rangofinal}: ${it.monto}%"
                    }
                    descuentosList.add(
                        DiscountItem(
                            type = "Escala",
                            rangoUnidades = rangosUnidades,
                            monto = 0.0,
                            promocion = descuentosEscala.first().promocion,
                            fechaInicio = descuentosEscala.first().fechaInicio ?: "-",
                            fechaFin = descuentosEscala.first().fechaFin ?: "-"
                        )
                    )
                }

                // Agregar descuentos por tipo de venta
                descuentosTipoVenta.forEach {
                    descuentosList.add(
                        DiscountItem(
                            type = "Tipo de Venta",
                            rangoUnidades = "",
                            monto = it.monto,
                            promocion = it.promocion,
                            fechaInicio = it.fechaInicio ?: "-",
                            fechaFin = it.fechaFin ?: "-"
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    if (descuentosList.isNotEmpty()) {
                        recyclerViewDescuentos.adapter = DiscountsAdapter(descuentosList)
                    } else {
                        Toast.makeText(
                            context,
                            "No hay descuentos disponibles",
                            Toast.LENGTH_LONG
                        ).show()
                        dismiss()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al cargar descuentos", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
