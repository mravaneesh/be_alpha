package com.example.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.ui.databinding.FragmentDialogConfirmationBinding

class DialogConfirmation(
    private val message: String,
    private val positiveText: String = "Yes",
    private val negativeText: String = "Cancel",
    private val onPositiveClick: (() -> Unit)? = null,
    private val onNegativeClick: (() -> Unit)? = null,
) : DialogFragment() {

    private var _binding: FragmentDialogConfirmationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        _binding = FragmentDialogConfirmationBinding.inflate(layoutInflater)
        builder.setView(binding.root)
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawableResource(com.example.utils.R.drawable.dialog_bg)

        binding.textMessage.text = message
        binding.buttonPositive.text = positiveText
        binding.buttonNegative.text = negativeText

        binding.buttonPositive.setOnClickListener {
            onPositiveClick?.invoke()
            dismiss()
        }

        binding.buttonNegative.setOnClickListener {
            onNegativeClick?.invoke()
            dismiss()
        }

        return dialog
    }
}
