package com.example.profile_ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.example.utils.Prefs
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.example.designsystem.theme.PactTheme
import com.example.profile_ui.R
import com.example.profile_ui.compose.ProfileScreen
import com.example.profile_ui.viewmodel.EditProfileViewModel
import com.example.profile_ui.viewmodel.ProfileViewModel
import com.example.utils.CommonFun

class ProfileFragment : Fragment() {
    private val viewModel: ProfileViewModel by activityViewModels()
    private val editProfileViewModel: EditProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PactTheme {
                    val state by viewModel.profile.collectAsStateWithLifecycle()
                    val stats by viewModel.stats.collectAsStateWithLifecycle()

                    // Keep the edit screen prefilled with the latest profile.
                    LaunchedEffect(state) {
                        if (!state.isLoading && state.error.isBlank()) {
                            editProfileViewModel.setUserProfile(state.profile)
                        }
                    }

                    val ctx = LocalContext.current
                    var runTour by remember { mutableStateOf(!Prefs.isScreenTourSeen(ctx, "profile")) }
                    ProfileScreen(
                        profile = state.profile,
                        stats = stats,
                        isLoading = state.isLoading,
                        onEdit = { findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment) },
                        onSettings = { findNavController().navigate(R.id.action_profileFragment_to_settingsFragment) },
                        runTour = runTour,
                        onTourFinished = { Prefs.setScreenTourSeen(ctx, "profile", true); runTour = false },
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Idempotent — won't re-fetch on config change or tab return.
        viewModel.load(CommonFun.getCurrentUserId()!!)
    }
}
