package com.example.goal_ui.view


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.goal_domain.model.Goal
import com.example.goal_ui.adapter.GoalPagerAdapter
import com.example.goal_ui.databinding.FragmentGoalBinding
import com.example.goal_ui.viewmodel.GoalViewModel
import com.example.utils.CommonFun
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoalFragment : Fragment() {
    private val userId = CommonFun.getCurrentUserId()!!
    private val viewModel: GoalViewModel by viewModels()
    private lateinit var binding: FragmentGoalBinding
    private lateinit var adapter: GoalPagerAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGoalBinding.inflate(inflater,container,false)
        setupViewPagerAndTabs()

        binding.tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    Log.d("TabLayout", "Tab Selected: ${tab.text}") // Debugging log
                    binding.viewPager.setCurrentItem(it.position, true) // Ensure smooth scrolling
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        return binding.root
    }

    private fun setupViewPagerAndTabs() {
        adapter = GoalPagerAdapter(this)
        binding.viewPager.adapter = adapter


        // Link TabLayout and ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Overall"
                1 -> tab.text = "Habits"
                2 -> tab.text = "Track"
            }
        }.attach()



    }



    private fun onGoalClicked(goal: Goal) {
        // Handle goal item click (open details or edit)
    }

    private fun openAddGoalBottomSheet() {
        // Open bottom sheet to add goal (using GoalBottomSheetFragment)
    }
}