package com.example.goal_ui.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.goal_domain.model.Goal
import com.example.goal_ui.databinding.FragmentBottomSheetBinding
import com.example.utils.CommonFun
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class BottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val defaultCategories = mutableListOf("Habit", "Track", "Other")
    private val habitGoals = listOf("Meditation", "Workout", "Reading", "Other")
    private val trackGoals = listOf("Calories", "Steps", "Water Intake", "Other")
    private val frequencies = listOf("Daily", "3x a week", "5x a week")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomSheetBinding.inflate(inflater, container, false)

        // Set Category Spinner Adapter
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, defaultCategories)
        binding.categorySpinner.adapter = categoryAdapter

        // Populate Frequency Spinner
        binding.frequencySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, frequencies)

        // Handle Category Selection
        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = defaultCategories[position]

                if (selectedCategory == "Other") {
                    binding.customCategoryInput.visibility = View.VISIBLE
                    binding.btnAddCategory.visibility = View.VISIBLE
                } else {
                    binding.customCategoryInput.visibility = View.GONE
                    binding.btnAddCategory.visibility = View.GONE
                    setupGoalSpinner(if (selectedCategory == "Habit") habitGoals else trackGoals)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Add New Category
        binding.btnAddCategory.setOnClickListener {
            val newCategory = binding.customCategoryInput.text.toString().trim()
            if (newCategory.isNotEmpty() && !defaultCategories.contains(newCategory)) {
                defaultCategories.add(defaultCategories.size - 1, newCategory) // Add before "Other"
                categoryAdapter.notifyDataSetChanged()
                binding.categorySpinner.setSelection(defaultCategories.indexOf(newCategory))
                binding.customCategoryInput.text.clear()
//                binding.customCategoryInput.visibility = View.GONE
//                binding.btnAddCategory.visibility = View.GONE
            } else {
                Toast.makeText(requireContext(), "Enter a valid category", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle Add Goal Click
        binding.btnAddGoal.setOnClickListener {
            val category = binding.categorySpinner.selectedItem.toString()
            val goal = if (binding.goalSpinner.selectedItem == "Other") binding.customGoalInput.text.toString() else binding.goalSpinner.selectedItem.toString()
            val frequency = binding.frequencySpinner.selectedItem.toString()

            if (goal.isNotEmpty()) {
                saveGoalToFirestore(category, goal, frequency)
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Please enter a goal", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
    private fun setupGoalSpinner(goals: List<String>) {
        binding.goalSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, goals)
        binding.customGoalInput.visibility = View.GONE
    }

    private fun saveGoalToFirestore(category: String, goal: String, frequency: String) {
        val db = FirebaseFirestore.getInstance()
        val goalId = UUID.randomUUID().toString()
        val goalDb = Goal(goalId, category, goal, frequency)
        val userId = CommonFun.getCurrentUserId()!!
        
        db.collection("goals")
            .document(userId) // User's unique document
            .collection(category) // Subcollection for goals
            .document(goalId) // Unique goal ID
            .set(goalDb)
            .addOnSuccessListener {
                Log.d("Firestore", "Goal successfully added")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding goal", e)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}