package com.example.goal_ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.goal_ui.view.pagerFragment.HabitFragment
import com.example.goal_ui.view.pagerFragment.OverallFragment
import com.example.goal_ui.view.pagerFragment.TrackFragment


class GoalPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3 // Track and Habit

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OverallFragment()
            1 -> HabitFragment()
            2 -> TrackFragment()
            else -> throw IllegalStateException("Unexpected position: $position")
        }
    }
}

