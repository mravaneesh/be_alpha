package com.example.goal_ui.view.pagerFragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.goal_domain.model.Goal
import com.example.goal_ui.R
import com.example.goal_ui.adapter.HabitGoalAdapter
import com.example.goal_ui.databinding.FragmentHabitBinding
import com.example.goal_ui.viewmodel.GoalViewModel
import com.example.utils.CommonFun
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HabitFragment : Fragment() {

    private val userId = CommonFun.getCurrentUserId()!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var binding: FragmentHabitBinding
    private val viewModel: GoalViewModel by  activityViewModels() // Shared ViewModel
    private lateinit var habitGoalAdapter: HabitGoalAdapter

    override fun onResume() {
        super.onResume()
        setupRecyclerView()
        fetchHabitGoals()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentHabitBinding.inflate(inflater, container, false)
        habitGoalAdapter = HabitGoalAdapter(
            onEditClick = { habit -> showEditDialog(habit) },
            openAnalytics = { habit -> showAnalytics(habit) },
            onDeleteClick = { habit -> deleteHabit(habit) }
        )
        return binding.root
    }

    private fun showAnalytics(habit: Goal) {
        val bundle = Bundle().apply {
            putParcelable("goal", habit)
        }
        requireParentFragment().findNavController()
            .navigate(R.id.action_goalFragment_to_habitAnalyticsFragment,bundle)
    }

    private fun showEditDialog(habit: Goal) {
        val bundle = bundleOf(
            "goalId" to habit.id,
            "title" to habit.title,
            "description" to habit.description,
            "color" to habit.color,
            "selectedDays" to habit.selectedDays,
            "reminder" to habit.reminder,
            "isEditMode" to true // To differentiate between Add and Edit
        )
        requireParentFragment().findNavController()
            .navigate(R.id.action_goalFragment_to_addGoalFragment, bundle)
    }

    private fun deleteHabit(habit: Goal) {
        db.collection("goals")
            .document(userId)
            .collection("Habit")
            .document(habit.id)
            .delete()
            .addOnSuccessListener {
                val updatedList = habitGoalAdapter.currentList.toMutableList()
                updatedList.remove(habit)
                habitGoalAdapter.submitList(updatedList)
                viewModel.loadHabitGoals(userId,"Habit")
                Toast.makeText(requireContext(), "Habit deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error deleting habit: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchHabitGoals() {
        viewModel.loadHabitGoals(userId,"Habit")
        observeData()
    }

    private fun setupRecyclerView() {
        binding.habitRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = habitGoalAdapter
        }
    }

    private fun observeData() {
        lifecycleScope.launch{
            viewModel.habitGoals.collectLatest { state ->
                when {
                    state.isLoading -> {
                        Toast.makeText(requireContext(), "Loading Habits...", Toast.LENGTH_SHORT).show()
                    }
                    state.error.isNotBlank() -> {
                        Toast.makeText(requireContext(), "Error: ${state.error}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        state.goals.let { goals ->
                            habitGoalAdapter.submitList(goals)
                            Log.d("HabitFragment", "Goals: $goals")
                        }
                    }
                }
            }
        }
    }

}