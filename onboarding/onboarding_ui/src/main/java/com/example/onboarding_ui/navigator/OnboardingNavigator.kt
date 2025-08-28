package com.example.onboarding_ui.navigator

import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.NavController
import com.example.onboarding_ui.R
import com.example.utils.CommonFun

class OnboardingNavigator(private val navController: NavController) {

    fun next(from: OnboardingStep) {
        when (from) {
            OnboardingStep.WELCOME ->
                navController.navigate(R.id.action_welcomeFragment_to_basicInfoFragment)

            OnboardingStep.BASIC_INFO ->
                navController.navigate(R.id.action_basicInfoFragment_to_goalSelectionFragment)

            OnboardingStep.GOALS ->
                navController.navigate(R.id.action_goalSelectionFragment_to_activityLevelFragment)

            OnboardingStep.ACTIVITY ->
                navController.navigate(R.id.action_activityLevelFragment_to_dietFragment)

            OnboardingStep.DIET ->
            {}
        }
    }

    fun back() {
        navController.popBackStack()
    }

//    fun finish() {
//        CommonFun.deepLinkNav("homeFragment",requireContext())
//    }
}