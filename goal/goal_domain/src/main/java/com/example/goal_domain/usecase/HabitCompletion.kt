package com.example.goal_domain.usecase

import com.example.goal_domain.model.Goal
import java.time.LocalDate

/**
 * Pure habit-completion maths shared by the app (GoalViewModel) and the home-screen widget, so the
 * progress/streak/success-rate rules can never drift between them. A habit is "done" for a day when
 * its [Goal.progress] holds 0 for that date; the streak only advances on a scheduled day.
 */
object HabitCompletion {

    /** Progress status codes stored in [Goal.progress]. */
    const val DONE = 0
    const val MISSED = 1
    const val PENDING = 3
    const val FROZEN = 4 // missed but a streak-freeze kept the streak alive

    /** Most freezes a habit can bank, and how many completions earn one. */
    const val MAX_FREEZES = 2
    private const val FREEZE_EARN_EVERY = 7

    /** True when [goal] is already marked complete for [date]. */
    fun isDoneOn(goal: Goal, date: LocalDate = LocalDate.now()): Boolean =
        (goal.progress[date.toString()] ?: PENDING) == DONE

    /** True when [goal] is scheduled to run on [date]. */
    fun isScheduledOn(goal: Goal, date: LocalDate = LocalDate.now()): Boolean =
        goal.selectedDays.contains(date.dayOfWeek.value % 7)

    /** Returns a copy of [goal] marked complete for [date] with streak/total/success-rate updated. */
    fun markComplete(goal: Goal, date: LocalDate = LocalDate.now()): Goal {
        val progress = goal.progress.toMutableMap()
        var currentStreak = goal.currentStreak
        var bestStreak = goal.bestStreak
        var totalCompleted = goal.totalCompleted
        var freezes = goal.freezesAvailable

        progress[date.toString()] = DONE
        if (isScheduledOn(goal, date)) {
            currentStreak++
            totalCompleted++
            if (currentStreak > bestStreak) bestStreak = currentStreak
            // Earn a freeze on each full-week streak milestone (capped).
            if (currentStreak % FREEZE_EARN_EVERY == 0 && freezes < MAX_FREEZES) freezes++
        }

        return goal.copy(
            progress = progress,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            totalCompleted = totalCompleted,
            successRate = successRate(totalCompleted, goal),
            freezesAvailable = freezes,
        )
    }

    /**
     * Inverse of [markComplete] for an accidental tap — only meant for *today*. Sets the day back to
     * pending and rolls back exactly what [markComplete] added (streak, total, the milestone freeze,
     * and a best-streak bump if this completion set the record).
     */
    fun markIncomplete(goal: Goal, date: LocalDate = LocalDate.now()): Goal {
        if (!isDoneOn(goal, date)) return goal
        val progress = goal.progress.toMutableMap()
        var currentStreak = goal.currentStreak
        var bestStreak = goal.bestStreak
        var totalCompleted = goal.totalCompleted
        var freezes = goal.freezesAvailable

        progress[date.toString()] = PENDING
        if (isScheduledOn(goal, date)) {
            // Reverse the milestone freeze first (this completion granted one iff it hit the milestone).
            if (currentStreak % FREEZE_EARN_EVERY == 0 && freezes > 0) freezes--
            val wasRecord = bestStreak == currentStreak
            currentStreak = (currentStreak - 1).coerceAtLeast(0)
            totalCompleted = (totalCompleted - 1).coerceAtLeast(0)
            if (wasRecord) bestStreak = (bestStreak - 1).coerceAtLeast(0)
        }

        return goal.copy(
            progress = progress,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            totalCompleted = totalCompleted,
            successRate = successRate(totalCompleted, goal),
            freezesAvailable = freezes,
        )
    }

    private fun successRate(totalCompleted: Int, goal: Goal): Int {
        val totalPossible = totalRequiredDays(goal.startDate, goal.selectedDays)
        return if (totalPossible > 0) (totalCompleted * 100) / totalPossible else 0
    }

    private fun totalRequiredDays(startDate: String, selectedDays: List<Int>): Int = runCatching {
        val start = LocalDate.parse(startDate)
        val today = LocalDate.now()
        var count = 0
        var current = start
        while (!current.isAfter(today)) {
            if (selectedDays.contains(current.dayOfWeek.value % 7)) count++
            current = current.plusDays(1)
        }
        count
    }.getOrDefault(0)
}
