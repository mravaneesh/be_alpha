package com.example.profile_ui.legal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.designsystem.components.LegalScreen
import com.example.designsystem.theme.PactTheme

class TermsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PactTheme {
                    LegalScreen(
                        title = "Terms of Service",
                        effectiveDate = LegalContent.EFFECTIVE_DATE,
                        sections = LegalContent.TERMS,
                        onBack = { findNavController().navigateUp() },
                    )
                }
            }
        }
}

class PrivacyFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PactTheme {
                    LegalScreen(
                        title = "Privacy Policy",
                        effectiveDate = LegalContent.EFFECTIVE_DATE,
                        sections = LegalContent.PRIVACY,
                        onBack = { findNavController().navigateUp() },
                    )
                }
            }
        }
}
