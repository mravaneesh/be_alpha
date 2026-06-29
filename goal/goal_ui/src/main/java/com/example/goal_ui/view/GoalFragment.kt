package com.example.goal_ui.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.example.utils.Prefs
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.designsystem.theme.PactTheme
import com.example.goal_domain.model.Goal
import com.example.goal_ui.R
import com.example.goal_ui.compose.HabitScreen
import com.example.goal_ui.databinding.FragmentGoalBinding
import com.example.goal_ui.state.GoalState
import com.example.goal_ui.viewmodel.GoalViewModel
import com.example.goal_ui.worker.HabitStatusFixer
import com.example.utils.CommonFun
import com.example.utils.reminder.HabitReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Habits tab. Hosts the Compose [HabitScreen] in a ComposeView placed directly in this fragment's
 * bounded layout (so the list scrolls); owns the shared [GoalViewModel] actions and navigation.
 */
@AndroidEntryPoint
class GoalFragment : Fragment() {

    private lateinit var binding: FragmentGoalBinding
    private val userId = CommonFun.getCurrentUserId()!!
    private val viewModel: GoalViewModel by activityViewModels()
    private var state by mutableStateOf(GoalState(isLoading = true))

    override fun onResume() {
        super.onResume()
        HabitStatusFixer.syncMissedAndPendingDays()
        viewModel.loadHabitGoals(userId, "Habit")
        observeData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGoalBinding.inflate(inflater, container, false)
        binding.ivMoreOptions.setOnClickListener {
            findNavController().navigate(R.id.action_goalFragment_to_addGoalFragment)
        }
        binding.habitCompose.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PactTheme {
                    val focusId by viewModel.focusHabitId.collectAsState()
                    val ctx = LocalContext.current
                    var runTour by remember { mutableStateOf(!Prefs.isScreenTourSeen(ctx, "habits")) }
                    HabitScreen(
                        runTour = runTour,
                        onTourFinished = { Prefs.setScreenTourSeen(ctx, "habits", true); runTour = false },
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
        return binding.root
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

    private fun showAnalytics(goalId: String) {
        findNavController().navigate(
            R.id.action_goalFragment_to_habitAnalyticsFragment, bundleOf("goalId" to goalId),
        )
    }

    private fun showEditDialog(habit: Goal) {
        val bundle = bundleOf(
            "goalId" to habit.id,
            "title" to habit.title,
            "description" to habit.description,
            "color" to habit.color,
            "selectedDays" to habit.selectedDays,
            "reminder" to habit.reminder,
            "isEditMode" to true,
        )
        findNavController().navigate(R.id.action_goalFragment_to_addGoalFragment, bundle)
    }

    private fun deleteHabit(habit: Goal) {
        viewModel.deleteHabit(userId, habit)
        HabitReminderScheduler.cancel(requireContext(), habit.id)
        Toast.makeText(requireContext(), "Habit deleted", Toast.LENGTH_SHORT).show()
        Log.i("GoalFragment", "deleted ${habit.id}")
    }
}
