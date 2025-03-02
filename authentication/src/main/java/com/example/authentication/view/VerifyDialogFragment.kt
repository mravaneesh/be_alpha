package com.example.authentication.view

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.authentication.R


class VerifyEmailDialog(
    private val onVerified: () -> Unit,
    private val onCancel: () -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_verify_dialog, null)

        builder.setView(view)

        val btnVerify = view.findViewById<TextView>(R.id.btnVerify)
        val btnCancel = view.findViewById<TextView>(R.id.btnCancel)

        btnVerify.setOnClickListener {
            dismiss()
            onVerified()
        }

        btnCancel.setOnClickListener {
            dismiss()
            onCancel()
        }

        val dialog = builder.create()

        // Apply rounded corners to the dialog window
        dialog.window?.setBackgroundDrawableResource(com.example.utils.R.drawable.dialog_bg)

        return dialog
    }
}
