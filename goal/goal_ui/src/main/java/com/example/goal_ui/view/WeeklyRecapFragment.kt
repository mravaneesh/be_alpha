package com.example.goal_ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.designsystem.theme.PactTheme
import com.example.goal_ui.compose.WeeklyRecapScreen
import com.example.goal_ui.state.GoalState
import com.example.goal_ui.viewmodel.GoalViewModel
import com.example.utils.CommonFun
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WeeklyRecapFragment : Fragment() {

    private val userId = CommonFun.getCurrentUserId()!!
    private val viewModel: GoalViewModel by activityViewModels()
    private var state by mutableStateOf(GoalState(isLoading = true))

    override fun onResume() {
        super.onResume()
        viewModel.loadHabitGoals(userId, "Habit")
        lifecycleScope.launch {
            viewModel.habitGoals.collectLatest { state = it }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PactTheme {
                    WeeklyRecapScreen(
                        goals = state.goals,
                        isLoading = state.isLoading,
                        onBack = { requireActivity().onBackPressedDispatcher.onBackPressed() },
                    )
                }
            }
        }
    }
}
