package com.example.goal_domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class Goal(
    val id: String = "",
    val category: String = "",
    val title: String = "",
    val description: String = "",
    val selectedDays: List<Int> = listOf(0, 1, 2, 3, 4, 5, 6),
    val color: Int = -1,
    val reminder: String = "",
    val startDate: String = "",
    val progress: Map<String, Int> = mapOf(LocalDate.now().toString() to 3),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompleted: Int = 0,
    val successRate: Int = 0,
    /** Streak freezes left for this habit; a missed scheduled day consumes one instead of resetting the streak. */
    val freezesAvailable: Int = 1,
    /** Whether this habit's progress counts toward what friends see (feed/leaderboard). */
    val shared: Boolean = true,
    /** Non-blank when this habit is tied to a group challenge; completing it is the daily check-in. */
    val challengeId: String = ""
): Parcelable

