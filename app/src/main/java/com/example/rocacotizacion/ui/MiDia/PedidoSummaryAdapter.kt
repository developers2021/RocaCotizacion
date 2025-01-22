package com.example.rocacotizacion.ui.MiDia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rocacotizacion.DataModel.PedidoSummary
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat


class PedidoSummaryAdapter(
    private var items: MutableList<PedidoSummary>,
    private val clickListener: (PedidoSummary) -> Unit
) : RecyclerView.Adapter<PedidoSummaryAdapter.PedidoSummaryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoSummaryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pedido_summary, parent, false)
        return PedidoSummaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoSummaryViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, clickListener)

        holder.tvCodigoPedido.text = "${item.codigopedido}"
        holder.tvTipopago.text = when (item.tipopago) {
            "CTADO" -> "CONTADO"
            "CRED" -> "CRÉDITO"
            else -> item.tipopago
        }
        holder.tvTotal.text = "L.${formatNumberWithCommas(item.total)}"

        CoroutineScope(Dispatchers.IO).launch {
            val nombreCliente = obtenerNombreCliente(holder.itemView.context, item.Codigocliente)
            withContext(Dispatchers.Main) {
                holder.tvNombreCliente.text = "$nombreCliente"
            }
        }

        holder.tvSinc.text = if (item.sinc) "Sincronizado" else "No Sincronizado"
        holder.tvSinc.setTextColor(
            if (item.sinc) holder.itemView.context.getColor(R.color.verde_roca)
            else holder.itemView.context.getColor(android.R.color.black)
        )

        if (item.anulado == "S") {
            holder.tvEstadoAnulado.visibility = View.VISIBLE
            holder.tvEstadoAnulado.text = "Anulado" // Opcional, puedes personalizar el texto
        } else {
            holder.tvEstadoAnulado.visibility = View.GONE
        }
    }


    override fun getItemCount(): Int = items.size

    class PedidoSummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(pedidoSummary: PedidoSummary, clickListener: (PedidoSummary) -> Unit) {
            itemView.setOnClickListener { clickListener(pedidoSummary) }
        }

        //val tvPedidoId: TextView = itemView.findViewById(R.id.tvPedidoId)
        val tvCodigoPedido: TextView = itemView.findViewById(R.id.tvCodigoPedido)
        val tvTipopago: TextView = itemView.findViewById(R.id.tvTipopago)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotal)
        val tvSinc: TextView = itemView.findViewById(R.id.tvSinc)
        val tvNombreCliente: TextView = itemView.findViewById(R.id.tvNombreCliente)
        val tvEstadoAnulado: TextView = itemView.findViewById(R.id.tvEstadoAnulado)
    }

    // Función para obtener el nombre del cliente usando Room
    private suspend fun obtenerNombreCliente(context: android.content.Context, codigoCliente: String): String {
        val cliente = DatabaseApplication.getDatabase(context)
            .ClientesDAO()
            .obtenerClientePorCodigo(codigoCliente)
        return cliente?.nombrecliente ?: "Cliente no encontrado"
    }

    // Función para formatear números con separador de miles
    fun formatNumberWithCommas(value: Double): String {
        val decimalFormat = DecimalFormat("#,##0.00")
        return decimalFormat.format(value)
    }

    fun updateItems(newItems: List<PedidoSummary>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
