package com.example.rocacotizacion.ui.home

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.rocacotizacion.DAO.Agente
import com.example.rocacotizacion.DAO.AppDatabase
import com.example.rocacotizacion.DAO.DatabaseApplication
import com.example.rocacotizacion.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Assuming you are storing the logged-in username in SharedPreferences
        val sharedPreferences = activity?.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val loggedInUsername = sharedPreferences?.getString("LoggedInUsername", null)

        // Execute the AsyncTask
        loggedInUsername?.let { username ->
            GetAgenteAsyncTask(requireContext(), username) { agente ->
                // This is your callback that gets executed on the main thread.
                // Update your UI here with the agent details.
                if (agente != null) {
                    val agentDetailsTextView = view.findViewById<TextView>(R.id.tvCodigoAgentes)
                    val details = "ID: ${agente.idAgentes}, Username: ${agente.username}"
                    agentDetailsTextView.text = details
                }
            }.execute()
        }
    }

    private class GetAgenteAsyncTask(
        private val context: Context,
        private val username: String,
        private val callback: (Agente?) -> Unit
    ) : AsyncTask<Void, Void, Agente?>() {

        private val db: AppDatabase = DatabaseApplication.getDatabase(context)

        override fun doInBackground(vararg params: Void?): Agente? {
            // Perform database operation in background
            return db.AgenteDAO().getAgenteByUsername(username)
        }

        override fun onPostExecute(result: Agente?) {
            super.onPostExecute(result)
            // Runs on the main thread, update your UI here
            callback(result)
        }
    }
}
