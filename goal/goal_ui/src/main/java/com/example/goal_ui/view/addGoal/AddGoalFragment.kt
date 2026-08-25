package com.example.goal_ui.view.addGoal

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.util.Calendar
import java.util.UUID

/**
 * Create / edit a habit. Hosts the Compose [AddGoalScreen].
 *
 * Creating goes through the repository, so a new habit lands in Room first and is uploaded by the
 * background sync pass. Editing still writes straight to Firestore — see [updateGoal].
 */
@AndroidEntryPoint
class AddGoalFragment : Fragment() {

    // Activity-scoped: saveGoal pops the back stack immediately, which would destroy a
    // fragment-scoped ViewModel and cancel the write coroutine with it.
    private val viewModel: GoalViewModel by activityViewModels()

    private val userId = CommonFun.getCurrentUserId()!!
    private val db = FirebaseFirestore.getInstance()
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
     * TODO: still a direct Firestore write, so an edit made offline is lost and Room only catches
     * up on the next refresh. Routing this through GoalRepository.updateGoalDetails() needs a
     * decision about the progress recompute below, which uses a status value (2) that
     * HabitCompletion does not model.
     */
    private fun updateGoal(goalId: String, title: String, description: String, days: List<Int>, color: Int, reminder: String) {
        val today = LocalDate.now().toString()
        val goalDocRef = db.collection("goals").document(userId).collection("Habit").document(goalId)
        goalDocRef.get()
            .addOnSuccessListener { document ->
                if (!document.exists()) return@addOnSuccessListener
                val currentProgressMap = mutableMapOf<String, Long>()
                (document.get("progress") as? Map<*, *>)?.forEach { (key, value) ->
                    if (key is String && value is Number) currentProgressMap[key] = value.toLong()
                }
                val oldStatus = currentProgressMap[today]?.toInt() ?: 3
                val todayDayIndex = LocalDate.now().dayOfWeek.value % 7
                val isTodaySelected = days.contains(todayDayIndex)
                if (!isTodaySelected && oldStatus == 3) currentProgressMap[today] = 2L
                else if (isTodaySelected && oldStatus == 2) currentProgressMap[today] = 3L

                goalDocRef.update(
                    mapOf(
                        "title" to title,
                        "description" to description,
                        "color" to color,
                        "reminder" to reminder,
                        "selectedDays" to days,
                        "progress" to currentProgressMap,
                    )
                ).addOnSuccessListener {
                    HabitReminderScheduler.schedule(requireContext(), goalId, title, reminder, days)
                    findNavController().popBackStack()
                }.addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to update goal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to fetch goal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
