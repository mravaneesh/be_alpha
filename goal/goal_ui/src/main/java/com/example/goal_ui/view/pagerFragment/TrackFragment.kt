package com.example.goal_ui.view.pagerFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.goal_domain.model.Goal
import com.example.goal_ui.adapter.TrackGoalAdapter
import com.example.goal_ui.databinding.FragmentTrackBinding
import com.example.goal_ui.viewmodel.GoalViewModel
import com.example.utils.CommonFun

class TrackFragment : Fragment() {
    private val userId = CommonFun.getCurrentUserId()!!
    private lateinit var binding: FragmentTrackBinding
    private val viewModel: GoalViewModel by activityViewModels() // Shared ViewModel
    private lateinit var trackGoalAdapter: TrackGoalAdapter

    override fun onResume() {
        super.onResume()
        setupRecyclerView()
//        fetchTrackGoals()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrackBinding.inflate(inflater,container,false)
        trackGoalAdapter = TrackGoalAdapter()
        return binding.root
    }

    private fun setupRecyclerView() {
        binding.trackRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(),2)
            adapter = trackGoalAdapter
        }

        val trackItems =
            mutableListOf(
                Goal("",title = "Steps", color = -1),
                Goal("",title = "Calorie", color = -1),
                Goal("",title = "Water Intake4", color = -1)
            )
        trackGoalAdapter.submitList(trackItems)
    }
//    private fun fetchTrackGoals() {
//        viewModel.loadTrackGoals(userId,"Track",requireContext())
//        observeData()
//    }
//
//    private fun observeData() {
//        lifecycleScope.launch{
//            viewModel.trackGoals.collectLatest { state ->
//                when {
//                    state.isLoading -> {
//                        Toast.makeText(requireContext(), "Loading Track...", Toast.LENGTH_SHORT).show()
//                    }
//                    state.error.isNotBlank() -> {
//                        Toast.makeText(requireContext(), "Error: ${state.error}", Toast.LENGTH_SHORT).show()
//                    }
//                    else -> {
//                        state.goals.let { goals ->
//                            trackGoalAdapter.submitList(goals) // Update RecyclerView with Habit goals
//                        }
//                    }
//                }
//            }
//        }
//    }

}