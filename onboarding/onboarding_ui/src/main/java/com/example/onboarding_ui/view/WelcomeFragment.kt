package com.example.onboarding_ui.view

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.onboarding_ui.R
import com.example.onboarding_ui.adapter.OnboardingAdapter
import com.example.onboarding_ui.databinding.FragmentWelcomeBinding
import com.example.onboarding_ui.navigator.OnboardingNavigator
import com.example.onboarding_ui.navigator.OnboardingStep
import com.example.utils.CommonFun
import com.example.utils.Prefs
import com.google.android.gms.common.internal.service.Common
import com.google.firebase.firestore.FirebaseFirestore

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = OnboardingAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false // disable swipe

        // Update progress on page change
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateStepProgress(position)
            }
        })

        updateStepProgress(0)
    }

    private fun updateStepProgress(step: Int) {
        val steps = listOf(binding.step1, binding.step2, binding.step3)
        steps.forEachIndexed { index, view ->
            view.setBackgroundResource(
                if (index <= step) com.example.utils.R.drawable.bg_progress_fill
                else R.drawable.bg_progress_track
            )
        }
    }

    fun goToNextPage() {
        val current = binding.viewPager.currentItem
        if (current < 1) {
            binding.viewPager.currentItem = current + 1
        } else {
            finishOnboarding()
        }
    }

    fun goToPreviousPage() {
        val current = binding.viewPager.currentItem
        if (current > 0) {
            binding.viewPager.currentItem = current - 1
        }
    }


//    private fun animateStepProgress(step: Int) {
//        binding.customProgress.post {
//            val parentWidth = (binding.customProgress.parent as View).width
//            val targetWidth = parentWidth * step / 3
//
//            ValueAnimator.ofInt(binding.customProgress.width, targetWidth).apply {
//                duration = 400
//                addUpdateListener {
//                    binding.customProgress.layoutParams.width = it.animatedValue as Int
//                    binding.customProgress.requestLayout()
//                }
//                start()
//            }
//        }
//    }

    private fun finishOnboarding() {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(CommonFun.getCurrentUserId()!!)
            .update("onboardingCompleted", true)
            .addOnSuccessListener {
                Prefs.setOnboardingCompleted(requireContext(), true)

                CommonFun.deepLinkNav("homeFragment",requireContext())
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}