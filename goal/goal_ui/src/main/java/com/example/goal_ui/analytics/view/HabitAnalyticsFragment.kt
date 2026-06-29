package com.example.goal_ui.analytics.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.designsystem.theme.PactTheme
import com.example.goal_domain.model.Goal
import com.example.goal_ui.compose.HabitAnalyticsScreen
import com.example.goal_ui.viewmodel.GoalViewModel
import com.example.utils.CommonFun
import com.example.utils.CommonFun.getGoalById
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Per-habit analytics. Fetches the goal by id and hosts the Compose [HabitAnalyticsScreen];
 * keeps the goal's stats fresh from the shared [GoalViewModel].
 */
class HabitAnalyticsFragment : Fragment() {

    private val viewModel: GoalViewModel by activityViewModels()
    private var goal by mutableStateOf<Goal?>(null)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val goalId = arguments?.getString("goalId")
        lifecycleScope.launch {
            if (goalId == null) {
                Log.e("HabitAnalytics", "goalId missing")
                return@launch
            }
            goal = getGoalById<Goal>(goalId)
            // Keep stats fresh from the shared view model when it emits.
            viewModel.habitGoals.collectLatest { state ->
                state.goals.find { it.id == goalId }?.let { goal = it }
            }
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PactTheme {
                    goal?.let { g ->
                        HabitAnalyticsScreen(
                            modifier = Modifier.systemBarsPadding(),
                            goal = g,
                            onBack = { findNavController().navigateUp() },
                        )
                    }
                }
            }
        }
    }
}
