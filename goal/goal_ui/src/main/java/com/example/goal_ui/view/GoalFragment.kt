package com.example.goal_ui.view
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.goal_domain.model.Goal
import com.example.goal_ui.R
import com.example.goal_ui.adapter.GoalPagerAdapter
import com.example.goal_ui.databinding.FragmentGoalBinding
import com.example.goal_ui.view.addGoal.AddTrackGoalBottomSheetFragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoalFragment : Fragment() {
    private lateinit var binding: FragmentGoalBinding
    private lateinit var adapter: GoalPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGoalBinding.inflate(inflater,container,false)
        setupViewPagerAndTabs()

        binding.ivMoreOptions.setOnClickListener {
            val currentFragment = binding.viewPager.currentItem
            if(currentFragment == 0)
            findNavController().navigate(R.id.action_goalFragment_to_addGoalFragment)
            else{
                val bottomSheet = AddTrackGoalBottomSheetFragment()
                bottomSheet.show(parentFragmentManager, bottomSheet.tag)
            }
        }
        return binding.root
    }

    private fun setupViewPagerAndTabs() {
        adapter = GoalPagerAdapter(this)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Habit"
                1 -> tab.text = "Track"
            }
        }.attach()
    }


}