package com.example.goal_ui.view.addGoal

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.goal_domain.model.Goal
import com.example.goal_ui.R
import com.example.goal_ui.databinding.BottomSheetTrackGoalBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.UUID

class AddTrackGoalBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: BottomSheetTrackGoalBinding? = null
    private val binding get() = _binding!!
    private lateinit var colorAdapter: TrackColorAdapter
    private lateinit var recyclerView: RecyclerView
    private val viewModel: AddGoalViewModel by activityViewModels()
    private var selectedColor: Int = Color.BLACK  // Default color
    private val colors = listOf(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetTrackGoalBinding.inflate(inflater, container, false)
        recyclerView = binding.rvColorPicker

        binding.btnSaveGoal.setOnClickListener {
            val title = binding.etGoalTitle.text.toString().trim()
            val description = binding.etGoalDescription.text.toString().trim()
            val target = binding.etTarget.text.toString().trim()
            val unit = binding.etUnit.text.toString().trim()

            if (title.isEmpty() || target.isEmpty()) {
                Toast.makeText(requireContext(), "Title and Target are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save to Firestore or pass back to ViewModel
            val newGoal = Goal(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                target = target,
                unit = unit,
                color = selectedColor
            )

            // Pass newGoal to ViewModel or Firestore logic
            dismiss()  // Close bottom sheet
        }
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

        colorAdapter = TrackColorAdapter(colors) { selectedColor ->
            viewModel.setTrackColor(selectedColor)
            dismiss()
        }
        recyclerView.adapter = colorAdapter
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
