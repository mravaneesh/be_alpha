package com.example.goal_ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.goal_ui.view.pagerFragment.HabitFragment
import com.example.goal_ui.view.pagerFragment.TrackFragment


class GoalPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    // Habit-only for the current design scope; Track is now its own top-level "Stats" tab.
    override fun getItemCount(): Int = 1

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HabitFragment()
            // 1 -> TrackFragment()  // moved to the Stats bottom-nav tab
            else -> throw IllegalStateException("Unexpected position: $position")
        }
    }
}

