package com.example.goal_ui.view.addGoal

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.designsystem.theme.PactTheme
import com.example.goal_domain.model.Goal
import com.example.goal_ui.compose.AddGoalScreen
import com.example.goal_ui.viewmodel.GoalViewModel
import com.example.utils.CommonFun
import com.example.utils.reminder.HabitReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.util.Calendar
import java.util.UUID

/**
 * Create / edit a habit. Hosts the Compose [AddGoalScreen].
 *
 * Both creating and editing go through the repository, so a habit lands in Room first and is
 * uploaded by the background sync pass — this screen never talks to Firestore.
 */
@AndroidEntryPoint
class AddGoalFragment : Fragment() {

    // Activity-scoped: saveGoal pops the back stack immediately, which would destroy a
    // fragment-scoped ViewModel and cancel the write coroutine with it.
    private val viewModel: GoalViewModel by activityViewModels()

    private val userId = CommonFun.getCurrentUserId()!!
    private val category = "Habit"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val isEdit = arguments?.getBoolean("isEditMode", false) ?: false
        val goalId = arguments?.getString("goalId") ?: ""
        val initialTitle = arguments?.getString("title") ?: ""
        val initialDescription = arguments?.getString("description") ?: ""
        val initialColor = arguments?.getInt("color") ?: 0
        val initialDays = (arguments?.getIntegerArrayList("selectedDays") ?: arrayListOf()).toSet()
        val initialReminder = arguments?.getString("reminder") ?: ""

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PactTheme {
                    AddGoalScreen(
                        modifier = Modifier.systemBarsPadding(),
                        isEdit = isEdit,
                        initialTitle = initialTitle,
                        initialDescription = initialDescription,
                        initialDays = initialDays,
                        initialColor = initialColor,
                        initialReminder = initialReminder,
                        onPickTime = ::pickTime,
                        onBack = { findNavController().popBackStack() },
                        onSave = { title, description, days, color, reminder ->
                            if (isEdit) {
                                updateGoal(goalId, title, description, days, color, reminder)
                            } else {
                                saveGoal(title, description, days, color, reminder)
                            }
                        },
                    )
                }
            }
        }
    }

    private fun pickTime(current: String, onPicked: (String) -> Unit) {
        val cal = Calendar.getInstance()
        runCatching {
            if (current.isNotBlank()) {
                val parsed = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).parse(current)
                if (parsed != null) cal.time = parsed
            }
        }
        TimePickerDialog(
            requireContext(),
            { _, hour, minute -> onPicked(CommonFun.formatTime(hour, minute)) },
            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false,
        ).show()
    }

    private fun saveGoal(title: String, description: String, days: List<Int>, color: Int, reminder: String) {
        val goalId = UUID.randomUUID().toString()
        val startDate = LocalDate.now().toString()
        val goal = Goal(goalId, category, title, description, days, color, reminder, startDate)
        viewModel.createGoal(userId, goal)
        HabitReminderScheduler.schedule(requireContext(), goalId, title, reminder, days)
        findNavController().popBackStack()
    }

    /**
     * The edit path no longer fetches the document, recomputes it, and writes it back: the
     * repository re-reads the freshest row inside a transaction and only overwrites the fields the
     * form owns, so the progress map is never round-tripped through this screen at all.
     *
     * The old version also stamped today's progress with status 2 ("off today") when the user
     * removed today from the schedule. That is dropped: every reader — analytics, streaks, the habit
     * card — already derives "is this day scheduled" from selectedDays, so the stored 2 duplicated
     * the schedule and could only ever be written for *today*, in the one moment an edit happened.
     */
    private fun updateGoal(goalId: String, title: String, description: String, days: List<Int>, color: Int, reminder: String) {
        viewModel.updateGoal(userId, goalId, title, description, days, color, reminder)
        HabitReminderScheduler.schedule(requireContext(), goalId, title, reminder, days)
        findNavController().popBackStack()
    }
}
