package com.example.goal_ui.view.addGoal

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.goal_ui.R
import com.example.goal_ui.adapter.ColorAdapter
import com.example.goal_ui.databinding.FragmentSelectionDialogBinding
import com.example.goal_ui.viewmodel.AddGoalViewModel
import java.util.Locale

class SelectionDialogFragment : DialogFragment() {
    private var _binding: FragmentSelectionDialogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddGoalViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentSelectionDialogBinding.inflate(layoutInflater)
        val selectionType = arguments?.getString("selection")
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setCancelable(true)
            .create()
            .apply {
                setOnShowListener {
                    when (selectionType) {
                        "color" -> showColorPicker()
                        "reminder" -> showTimePicker()
                    }
                }
            }
    }

    private fun showColorPicker() {
        val colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.color1),
            ContextCompat.getColor(requireContext(), R.color.color2),
            ContextCompat.getColor(requireContext(), R.color.color3),
            ContextCompat.getColor(requireContext(), R.color.color4),
            ContextCompat.getColor(requireContext(), R.color.color5),
            ContextCompat.getColor(requireContext(), R.color.color6),
            ContextCompat.getColor(requireContext(), R.color.color7)
        )

        binding.colorRecyclerView.apply {
            visibility = View.VISIBLE
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = ColorAdapter(colors) { selectedColor ->
                viewModel.setColor(selectedColor)
                dismiss()
            }
        }

        binding.timePicker.visibility = View.GONE
        binding.btnConfirmTime.visibility = View.GONE
    }

    private fun showTimePicker() {
        binding.colorRecyclerView.visibility = View.GONE
        binding.timePicker.visibility = View.VISIBLE
        binding.btnConfirmTime.visibility = View.VISIBLE

        binding.timePicker.setOnTimeChangedListener { _, hour, minute ->
            val displayHour = when {
                hour == 0 -> 12 // Midnight
                hour == 12 -> 12 // Noon
                hour > 12 -> hour - 12
                else -> hour
            }
            val formattedTime = String.format(
                Locale.getDefault(), "%02d:%02d %s",
                displayHour, minute, if (hour >= 12) "PM" else "AM"
            )

            viewModel.setTime(formattedTime)
        }

        binding.btnConfirmTime.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}