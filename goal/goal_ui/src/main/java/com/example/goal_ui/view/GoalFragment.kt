package com.example.goal_ui.view


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.goal_domain.model.Goal
import com.example.goal_ui.adapter.CategoryAdapter
import com.example.goal_ui.adapter.GoalAdapter
import com.example.goal_ui.databinding.FragmentGoalBinding
import com.example.goal_ui.viewmodel.GoalViewModel
import com.example.utils.CommonFun
import com.example.utils.ProgressDialogUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class GoalFragment : Fragment() {
    private val userId = CommonFun.getCurrentUserId()!!
    private val viewModel: GoalViewModel by viewModels()
    private lateinit var binding: FragmentGoalBinding
    private lateinit var adapter: CategoryAdapter
    private lateinit var goalAdapter: GoalAdapter
    private lateinit var goalRecyclerView: RecyclerView
    private val categories = mutableListOf("All","Habit", "Track")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGoalBinding.inflate(inflater,container,false)
        goalRecyclerView = binding.goalsRecyclerView
        goalRecyclerView.layoutManager = GridLayoutManager(context, 2) // 2 items per row
        goalAdapter = GoalAdapter() // Initialize with an empty list
        goalRecyclerView.adapter = goalAdapter

        val recyclerView = binding.categoryRecyclerView

        // Set LayoutManager for horizontal scrolling
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        // Set the adapter
        adapter = CategoryAdapter(categories) { category ->
            viewModel.loadGoals(userId,category,requireContext())
            setObserver()
        }
        recyclerView.adapter = adapter

        binding.fabAddGoal.setOnClickListener{
            val bottomSheet = BottomSheetFragment()
            bottomSheet.show(parentFragmentManager, "BottomSheet")
        }
        return binding.root
    }

    private fun setObserver() {
        lifecycleScope.launchWhenStarted {
            viewModel.goals.collectLatest {
                if(it.isLoading){
                    ProgressDialogUtil.showProgressDialog(requireContext())
                }
                if(it.error.isNotBlank()){
                }
                it.goals.let{
                    Log.d("TAG", "setObserver: $it")
                    goalAdapter.submitList(it)
                }
            }
        }
    }


    private fun onGoalClicked(goal: Goal) {
        // Handle goal item click (open details or edit)
    }

    private fun openAddGoalBottomSheet() {
        // Open bottom sheet to add goal (using GoalBottomSheetFragment)
    }


     fun addNewCategory(newCategory: String) {
        adapter.addCategory(newCategory)
    }
}