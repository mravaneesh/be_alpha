package com.example.goal_ui.view.addGoal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.TimePicker
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.goal_ui.R
import com.example.goal_ui.databinding.FragmentBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Locale

class BottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var colorAdapter: ColorAdapter
    private lateinit var recyclerView: RecyclerView
    private val viewModel: AddGoalViewModel by activityViewModels()

    private lateinit var timePicker: TimePicker
    private lateinit var tvSelectedTime: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomSheetBinding.inflate(inflater, container, false)
        recyclerView = binding.colorRecyclerView
        timePicker = binding.timePicker
        tvSelectedTime = requireActivity().findViewById(R.id.tvSelectedTime)

        val selection = arguments?.getString("selection")
        if (selection == "color") {
            showColorPicker()
        } else {
            showTimePicker()
        }

        setupTimePickerListener()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.color4),
            ContextCompat.getColor(requireContext(), R.color.color7),
            ContextCompat.getColor(requireContext(), R.color.color1),
            ContextCompat.getColor(requireContext(), R.color.color2),
            ContextCompat.getColor(requireContext(), R.color.color3),
            ContextCompat.getColor(requireContext(), R.color.color5),
            ContextCompat.getColor(requireContext(), R.color.color6)
        )
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        colorAdapter = ColorAdapter(colors) { selectedColor ->
            viewModel.setColor(selectedColor)
            dismiss()
        }
        recyclerView.adapter = colorAdapter

    }

    private fun setupTimePickerListener() {
        timePicker.setOnTimeChangedListener { _, hour, minute ->
            val formattedTime = String.format(
                Locale.getDefault(), "%02d:%02d %s",
                if (hour > 12) hour - 12 else hour, minute, if (hour >= 12) "PM" else "AM")

            tvSelectedTime.text = formattedTime
            viewModel.setTime(formattedTime)
        }
    }

    private fun showColorPicker() {
        recyclerView.visibility = View.VISIBLE
        timePicker.visibility = View.GONE
    }

    private fun showTimePicker() {
        recyclerView.visibility = View.GONE
        timePicker.visibility = View.VISIBLE
    }
}