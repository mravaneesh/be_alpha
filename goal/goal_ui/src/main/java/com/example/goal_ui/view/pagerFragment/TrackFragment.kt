package com.example.goal_ui.view.pagerFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.goal_ui.adapter.GoalAdapter
import com.example.goal_ui.databinding.FragmentTrackBinding
import com.example.goal_ui.viewmodel.GoalViewModel
import kotlinx.coroutines.flow.collectLatest

class TrackFragment : Fragment() {

    private lateinit var binding: FragmentTrackBinding
    private val viewModel: GoalViewModel by activityViewModels() // Shared ViewModel
    private val goalAdapter = GoalAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrackBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
    }

    private fun setupRecyclerView() {
        binding.trackRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = goalAdapter
        }
    }

    private fun observeData() {
        lifecycleScope.launchWhenStarted {
            viewModel.goals.collectLatest { state ->
                if (state.isLoading) {
                    // Show loading state
                } else if (state.error.isNotBlank()) {
                    // Handle error
                } else {
                    val trackGoals = state.goals.filter { it.category == "Track" }
                    goalAdapter.submitList(trackGoals)
                }
            }
        }
    }

}