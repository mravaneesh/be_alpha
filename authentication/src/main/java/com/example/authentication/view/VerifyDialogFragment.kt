package com.example.authentication.view

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.authentication.R
import com.example.utils.CommonFun


class VerifyEmailDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_verify_dialog, null)

        builder.setView(view)

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)  // Disables dismiss on outside touch
        dialog.setCancelable(false)
        // Intercept back button press
        dialog.setOnKeyListener { _, keyCode, _ ->
            keyCode == android.view.KeyEvent.KEYCODE_BACK
        }

        // Optional rounded corners
        dialog.window?.setBackgroundDrawableResource(com.example.utils.R.drawable.dialog_bg)

        // Auto dismiss after 3 seconds
        dialog.setOnShowListener {
            val nextScreen = "onboarding"
            Log.i("VerifyEmailDialog", "nextScreen: $nextScreen")
            view.postDelayed({
                dismiss()
                CommonFun.deepLinkNav(nextScreen, requireContext())
            }, 3000)
        }

        return dialog
    }
}
