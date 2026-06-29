package com.example.bealpha_.widget

import android.content.Context
import com.example.goal_domain.repository.GoalRepository
import com.example.goal_domain.usecase.HabitCompletion
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** Hilt bridge so the widget (not a @AndroidEntryPoint) can reach the offline-first repository. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun goalRepository(): GoalRepository
}

enum class WidgetUiState { EMPTY, POPULATED, ALL_DONE }

data class WidgetHabit(
    val id: String,
    val title: String,
    val category: String,
    val done: Boolean,
)

data class WeekBar(val label: String, val ratio: Float, val isToday: Boolean)

data class WidgetData(
    val state: WidgetUiState,
    val percent: Int,
    val doneCount: Int,
    val totalCount: Int,
    val streak: Int,
    val greeting: String,
    val dateLabel: String,
    val habits: List<WidgetHabit>,
    val week: List<WeekBar>,
) {
    companion object {
        val EMPTY = WidgetData(
            state = WidgetUiState.EMPTY, percent = 0, doneCount = 0, totalCount = 0,
            streak = 0, greeting = "", dateLabel = "", habits = emptyList(), week = emptyList(),
        )
    }
}

private fun repository(context: Context): GoalRepository =
    EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java).goalRepository()

/**
 * Snapshot today's habits for the widget straight from the Room cache (offline-first source of
 * truth). We intentionally do NOT refresh from the network here: a network pull could land between
 * a tap-to-toggle write and this read and clobber the optimistic local change, making the widget
 * look like it ignored the tap. The app keeps Room fresh; the widget just mirrors it.
 */
suspend fun loadWidgetData(context: Context): WidgetData {
    val goals = runCatching { repository(context).observeGoals("Habit").first() }.getOrDefault(emptyList())
    return buildWidgetData(goals)
}

/** Re-read a single habit from the cache so an action can mutate it. */
suspend fun findHabit(context: Context, habitId: String) =
    runCatching { repository(context).observeGoals("Habit").first().firstOrNull { it.id == habitId } }
        .getOrNull()

/** Toggle today's completion: complete if not done, undo if already done (accidental tap). */
suspend fun toggleHabit(context: Context, goal: com.example.goal_domain.model.Goal) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val updated = if (HabitCompletion.isDoneOn(goal)) HabitCompletion.markIncomplete(goal)
    else HabitCompletion.markComplete(goal)
    repository(context).updateGoal(uid, updated)
}

private fun buildWidgetData(goals: List<com.example.goal_domain.model.Goal>): WidgetData {
    val today = LocalDate.now()
    val todayIdx = today.dayOfWeek.value % 7
    val scheduled = goals.filter { it.selectedDays.contains(todayIdx) }

    if (goals.isEmpty() || scheduled.isEmpty()) return WidgetData.EMPTY

    val total = scheduled.size
    val done = scheduled.count { HabitCompletion.isDoneOn(it, today) }
    val percent = if (total == 0) 0 else done * 100 / total
    val streak = goals.maxOfOrNull { it.currentStreak } ?: 0

    val state = when {
        done >= total -> WidgetUiState.ALL_DONE
        else -> WidgetUiState.POPULATED
    }

    val habits = scheduled.map {
        WidgetHabit(it.id, it.title, it.category, HabitCompletion.isDoneOn(it, today))
    }

    return WidgetData(
        state = state,
        percent = percent,
        doneCount = done,
        totalCount = total,
        streak = streak,
        greeting = greetingFor(),
        dateLabel = "${today.dayOfWeek.shortName()} · ${today.month.shortName()} ${today.dayOfMonth} · $done/$total",
        habits = habits,
        week = computeWeek(goals, today),
    )
}

private fun computeWeek(goals: List<com.example.goal_domain.model.Goal>, today: LocalDate): List<WeekBar> {
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")
    val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
    return (0..6).map { i ->
        val date = monday.plusDays(i.toLong())
        val idx = date.dayOfWeek.value % 7
        val sched = goals.filter { it.selectedDays.contains(idx) }
        val ratio = if (sched.isEmpty()) 0f
        else sched.count { HabitCompletion.isDoneOn(it, date) }.toFloat() / sched.size
        WeekBar(labels[i], ratio.coerceIn(0f, 1f), date == today)
    }
}

private fun greetingFor(): String = when (java.time.LocalTime.now().hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    else -> "Good evening"
}

private fun DayOfWeek.shortName() =
    getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(Locale.getDefault())

private fun java.time.Month.shortName() =
    getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(Locale.getDefault())
