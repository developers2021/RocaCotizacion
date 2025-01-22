package com.example.rocacotizacion.ui.MiDia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.rocacotizacion.R
import java.text.NumberFormat
import java.util.Locale

class CierreDiaFragment : Fragment() {

    private lateinit var viewModel: PedidoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cierre_dia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(PedidoViewModel::class.java)

        // Inicializar TextViews con los IDs correctos
        val totalTextView: TextView = view.findViewById(R.id.textTotalValue)
        val contadoTextView: TextView = view.findViewById(R.id.textContadoValue)
        val creditoTextView: TextView = view.findViewById(R.id.textCreditoValue)

        // Observar datos de pedidoHdrList y calcular el resumen
        viewModel.pedidoHdrList.observe(viewLifecycleOwner) { pedidoHdrList ->
            var total = 0.0
            var contado = 0.0
            var credito = 0.0

            pedidoHdrList.forEach { pedido ->
                if (pedido.anulado == "S") {
                    return@forEach // Continuar al siguiente pedido
                }
                total += pedido.total
                when (pedido.tipopago) {
                    "CTADO" -> contado += pedido.total
                    "CRED" -> credito += pedido.total
                }
            }


            val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }


            totalTextView.text = "L ${numberFormat.format(total)}"
            contadoTextView.text = "Contado: L ${numberFormat.format(contado)}"
            creditoTextView.text = "Crédito: L ${numberFormat.format(credito)}"
        }
    }
}
