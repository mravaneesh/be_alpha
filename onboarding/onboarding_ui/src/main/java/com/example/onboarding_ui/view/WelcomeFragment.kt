package com.example.onboarding_ui.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.onboarding_ui.R
import com.example.onboarding_ui.databinding.FragmentWelcomeBinding
import com.example.onboarding_ui.navigator.OnboardingNavigator
import com.example.onboarding_ui.navigator.OnboardingStep

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    private val navigator by lazy { OnboardingNavigator(findNavController()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGetStarted.setOnClickListener {
            navigator.next(OnboardingStep.WELCOME)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}