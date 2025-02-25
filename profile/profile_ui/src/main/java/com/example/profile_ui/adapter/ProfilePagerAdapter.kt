package com.example.profile_ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.profile_ui.view.pagerFragment.ChallengesFragment
import com.example.profile_ui.view.pagerFragment.PostsFragment
import com.example.profile_ui.view.pagerFragment.StatisticsFragment

class ProfilePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3 // Number of tabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PostsFragment()        // Your first tab (Posts)
            1 -> StatisticsFragment()   // Your second tab (Statistics)
            2 -> ChallengesFragment()   // Your third tab (Challenges)
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}
