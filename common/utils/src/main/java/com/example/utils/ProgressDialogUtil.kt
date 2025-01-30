package com.example.utils

import android.app.Dialog
import android.content.Context

object ProgressDialogUtil {
    private var progressDialog: Dialog? = null

    fun showProgressDialog(context: Context) {
        if (progressDialog?.isShowing == true) return  // Prevent multiple dialogs

        progressDialog = Dialog(context)
        progressDialog?.setContentView(R.layout.dialog_progress)
        progressDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        progressDialog?.setCancelable(false)
        progressDialog?.show()
    }

    fun hideProgressDialog() {
        progressDialog?.dismiss()
    }
}