package com.example.onboarding_ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.onboarding_ui.view.BasicInfoFragment
import com.example.onboarding_ui.view.FinishFragment
import com.example.onboarding_ui.preferences.PreferencesFragment

class OnboardingAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 3 // three screens
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> BasicInfoFragment()
            1 -> PreferencesFragment()
            2 -> FinishFragment()
            else -> throw IllegalStateException("Invalid page")
        }
    }
}
