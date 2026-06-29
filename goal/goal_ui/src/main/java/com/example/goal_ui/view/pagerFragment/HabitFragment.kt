package com.example.goal_ui.view.pagerFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.designsystem.theme.PactTheme
import com.example.goal_domain.model.Goal
import com.example.goal_ui.R
import com.example.goal_ui.compose.HabitScreen
import com.example.goal_ui.state.GoalState
import com.example.goal_ui.viewmodel.GoalViewModel
import com.example.utils.CommonFun
import com.example.utils.CommonFun.getGoalById
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HabitFragment : Fragment() {

    private val userId = CommonFun.getCurrentUserId()!!
    private val db = FirebaseFirestore.getInstance()
    private val viewModel: GoalViewModel by activityViewModels() // Shared ViewModel

    private var state by mutableStateOf(GoalState(isLoading = true))

    override fun onResume() {
        super.onResume()
        fetchHabitGoals()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PactTheme {
                    val focusId by viewModel.focusHabitId.collectAsState()
                    HabitScreen(
                        goals = state.goals,
                        isLoading = state.isLoading,
                        onStatusChange = { goal -> viewModel.updateGoalAnalytics(userId, goal) },
                        onUndo = { goal -> viewModel.undoGoalAnalytics(userId, goal) },
                        onEdit = ::showEditDialog,
                        onDelete = ::deleteHabit,
                        onAnalytics = ::showAnalytics,
                        focusHabitId = focusId,
                        onFocusConsumed = { viewModel.focusHabit(null) },
                    )
                }
            }
        }
    }

    private fun showAnalytics(goalId: String) {
        lifecycleScope.launch {
            val goal = getGoalById<Goal>(goalId = goalId)
            if (goal != null) Log.i("HabitFragment", "Goal: ${goal.progress}")
            requireParentFragment().findNavController()
                .navigate(R.id.action_goalFragment_to_habitAnalyticsFragment, bundleOf("goalId" to goalId))
        }
    }

    private fun showEditDialog(habit: Goal) {
        val bundle = bundleOf(
            "goalId" to habit.id,
            "title" to habit.title,
            "description" to habit.description,
            "color" to habit.color,
            "selectedDays" to habit.selectedDays,
            "reminder" to habit.reminder,
            "isEditMode" to true
        )
        requireParentFragment().findNavController()
            .navigate(R.id.action_goalFragment_to_addGoalFragment, bundle)
    }

    private fun deleteHabit(habit: Goal) {
        viewModel.deleteHabit(userId, habit)
        com.example.utils.reminder.HabitReminderScheduler.cancel(requireContext(), habit.id)
        Toast.makeText(requireContext(), "Habit deleted", Toast.LENGTH_SHORT).show()
    }

    private fun fetchHabitGoals() {
        viewModel.loadHabitGoals(userId, "Habit")
        observeData()
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.habitGoals.collectLatest { newState ->
                state = newState
                if (newState.error.isNotBlank()) {
                    Toast.makeText(requireContext(), newState.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
