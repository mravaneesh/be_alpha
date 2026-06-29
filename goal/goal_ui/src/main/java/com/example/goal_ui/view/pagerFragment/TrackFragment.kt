package com.example.goal_ui.view.pagerFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.example.utils.Prefs
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.designsystem.theme.PactTheme
import com.example.goal_ui.compose.ProgressScreen
import com.example.goal_ui.state.GoalState
import com.example.goal_ui.viewmodel.GoalViewModel
import com.example.utils.CommonFun
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TrackFragment : Fragment() {

    private val userId = CommonFun.getCurrentUserId()!!
    private val viewModel: GoalViewModel by activityViewModels()
    private var state by mutableStateOf(GoalState(isLoading = true))

    override fun onResume() {
        super.onResume()
        viewModel.loadHabitGoals(userId, "Habit")
        observeData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PactTheme {
                    val ctx = LocalContext.current
                    var runTour by remember { mutableStateOf(!Prefs.isScreenTourSeen(ctx, "stats")) }
                    ProgressScreen(
                        goals = state.goals,
                        isLoading = state.isLoading,
                        onRecap = { CommonFun.navigateToDeepLinkFragment(findNavController(), "weeklyRecap") },
                        runTour = runTour,
                        onTourFinished = { Prefs.setScreenTourSeen(ctx, "stats", true); runTour = false },
                    )
                }
            }
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.habitGoals.collectLatest { newState -> state = newState }
        }
    }
}
