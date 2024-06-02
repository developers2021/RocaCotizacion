package com.example.rocacotizacion.Adapter
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import androidx.navigation.Navigation
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ConditionHandler {
    fun checkCondition(context: Context, callback: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = DatabaseApplication.getDatabase(context)
            val count = db.PedidoHdrDAO().getAllPedidoHdrS().size
            withContext(Dispatchers.Main) {
                callback(count == 0)
            }
        }
    }
    fun deletedbLogOut(context: Context){
        CoroutineScope(Dispatchers.IO).launch {
            // Ensure you get an instance of your Room database and then get the DAO instance
            val db = DatabaseApplication.getDatabase(context)
            // Clear the tables before inserting new data
            db.AgenteDAO().deleteAll()
            db.ClientesDAO().deleteAll()
            db.ProductosDAO().deleteAll()
            db.GrupoDAO().deleteAll()
            db.PreciosNivelTipoVentaDAO().deleteAll()
            db.NivelPrecioPredeterminadoDAO().deleteAll()
            db.PedidoDtlDAO().deleteAll()
            db.PedidoHdrDAO().deleteAll()
        }
    }
    fun clearAllSharedPreferences(context: Context) {
        val sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            clear()  // This will clear all entries
            apply()
        }
    }

    fun navigateToLogin(context: Context) {
        if (context is Activity) {
            val navController = Navigation.findNavController(context,R.id.nav_home)
            navController.navigate(R.id.nav_login)
        }
    }

    fun showConfirmationDialog(context: Context) {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setMessage("¿Está seguro que desea cerrar sesión?")
            .setCancelable(false)
            .setPositiveButton("Sí") { dialog, id ->
                checkCondition(context) { conditionResult ->
                    if (conditionResult) {
                        CoroutineScope(Dispatchers.IO).launch {
                            deletedbLogOut(context)
                            withContext(Dispatchers.Main) {
                                clearAllSharedPreferences(context)
                                navigateToLogin(context)
                            }
                        }
                    } else {
                        showResultDialog("No puede cerrar sesión, hay pedidos pendientes de sincronizar.", context)
                    }
                }
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
            }

        val alert = dialogBuilder.create()
        alert.setTitle("Alerta")
        alert.show()
    }


    private fun showResultDialog(message: String, context: Context) {
        AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, id ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
