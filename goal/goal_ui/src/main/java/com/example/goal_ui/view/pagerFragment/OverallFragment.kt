package com.example.goal_ui.view.pagerFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.goal_ui.adapter.HabitGoalAdapter
import com.example.goal_ui.adapter.TrackGoalAdapter
import com.example.goal_ui.databinding.FragmentOverallBinding
import com.example.goal_ui.viewmodel.GoalViewModel
import com.example.utils.CommonFun
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OverallFragment : Fragment() {
    private lateinit var binding: FragmentOverallBinding
    private val viewModel: GoalViewModel by activityViewModels()
    private lateinit var habitAdapter: HabitGoalAdapter
    private lateinit var trackAdapter: TrackGoalAdapter
    private val userId = CommonFun.getCurrentUserId()!!

    override fun onResume() {
        super.onResume()
        fetchHabitGoals()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOverallBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupButtons()
        return binding.root
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitGoalAdapter()
        trackAdapter = TrackGoalAdapter()

        binding.recyclerHabit.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = habitAdapter
        }

        binding.recyclerTrack.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trackAdapter
        }
    }

    private fun setupButtons() {
        binding.habitLayout.setOnClickListener {
            toggleVisibility(binding.recyclerHabit)
        }

        binding.trackLayout.setOnClickListener {
            toggleVisibility(binding.recyclerTrack)
        }
    }

    private fun toggleVisibility(view: View) {
        view.visibility = if (view.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }


    private fun fetchHabitGoals() {
        viewModel.loadHabitGoals(userId, "Habit", requireContext())
        viewModel.loadTrackGoals(userId, "Track", requireContext())

        observeGoals()
    }

    private fun observeGoals() {
        lifecycleScope.launch {
            viewModel.habitGoals.collectLatest { state ->
                state.goals.let { habitAdapter.submitList(it) }
            }
        }

        lifecycleScope.launch {
            viewModel.trackGoals.collectLatest { state ->
                state.goals.let { trackAdapter.submitList(it) }
            }
        }
    }
}