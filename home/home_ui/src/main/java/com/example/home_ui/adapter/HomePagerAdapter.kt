package com.example.home_ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.home_ui.view.DashboardFragment
import com.example.home_ui.view.FeedFragment

class HomePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2 // Track and Habit

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DashboardFragment()
            1 -> FeedFragment()
            else -> throw IllegalStateException("Unexpected position: $position")
        }
    }
}
