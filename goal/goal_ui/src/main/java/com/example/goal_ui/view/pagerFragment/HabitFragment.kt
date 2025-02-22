package com.example.goal_ui.view.pagerFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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
    private val habitGoalAdapter = HabitGoalAdapter()

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
        return binding.root
    }

    private fun fetchHabitGoals() {
        viewModel.loadHabitGoals(userId,"Habit",requireContext())
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
                            habitGoalAdapter.submitList(goals) // Update RecyclerView with Habit goals
                        }
                    }
                }
            }
        }
    }

}