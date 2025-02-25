package com.example.profile_ui.view.editProfile

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.profile_ui.R
import com.example.profile_ui.adapter.GenderAdapter
import com.example.profile_ui.databinding.FragmentBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar

class BottomSheetFragment() : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomSheetBinding? = null
    private val binding get() = _binding!!
    private var selectionType: String? = null
    private var initialValue: String? = null
    private var onSelectionMade: ((String) -> Unit)? = null

    companion object {
        fun newInstance(selection: String, initialValue: String?, callback: (String) -> Unit): BottomSheetFragment {
            return BottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString("selection", selection)
                    putString("initialValue", initialValue)
                }
                onSelectionMade = callback
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectionType = arguments?.getString("selection")
        initialValue = arguments?.getString("initialValue")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomSheetBinding.inflate(inflater, container, false)
        val datePicker = binding.datePicker
        val recyclerView = binding.recyclerViewGender

        if (selectionType == "birthday") {
            setupDatePicker(datePicker, recyclerView)
        } else if (selectionType == "gender") {
            setupGenderRecyclerView(recyclerView, datePicker)
        }
        return binding.root
    }

    private fun setupDatePicker(datePicker: DatePicker, recyclerView: RecyclerView) {
        datePicker.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        // Set initial date if available
        val calendar = Calendar.getInstance()
        initialValue?.split("/")?.let { parts ->
            if (parts.size == 3) {
                calendar.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
            }
        }
        datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        // On date change, update instantly
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)) { _, year, month, day ->
            val selectedDate = "$day/${month + 1}/$year"
            onSelectionMade?.invoke(selectedDate)
        }
    }

    private fun setupGenderRecyclerView(recyclerView: RecyclerView, datePicker: DatePicker) {
        datePicker.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        val genders = listOf("Male", "Female", "Other")
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = GenderAdapter(genders, initialValue) { selectedGender ->
            onSelectionMade?.invoke(selectedGender)
            dismiss()
        }
    }
}