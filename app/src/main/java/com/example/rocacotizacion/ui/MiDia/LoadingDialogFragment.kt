package com.example.rocacotizacion.ui

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.example.rocacotizacion.R

class LoadingDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_loading)
        dialog.setCancelable(false)
        return dialog
    }

    companion object {
        const val TAG = "LoadingDialogFragment"

        fun show(activity: FragmentActivity) {
            val fragment = LoadingDialogFragment()
            fragment.show(activity.supportFragmentManager, TAG)
        }

        fun dismiss(activity: FragmentActivity) {
            val fragment = activity.supportFragmentManager.findFragmentByTag(TAG) as? LoadingDialogFragment
            fragment?.dismiss()
        }
    }
}