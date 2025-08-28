package com.example.goal_ui.worker

import android.content.Context
import android.util.Log
import com.example.goal_domain.model.Goal
import com.example.goal_ui.state.HabitAnalyticsState
import com.example.utils.CommonFun
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

object HabitStatusFixer {

    private const val LAST_SYNC_PREF = "last_sync_date"

    fun syncIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences("habit_sync_prefs", Context.MODE_PRIVATE)
        val lastSyncDate = prefs.getString(LAST_SYNC_PREF, null)
        val today = LocalDate.now().toString()

        if (lastSyncDate != today) {
            syncMissedAndPendingDays()
            prefs.edit().putString(LAST_SYNC_PREF, today).apply()
        } else {
            Log.i("HabitFixer", "⏳ Sync skipped - already synced today.")
        }
    }

    fun syncMissedAndPendingDays() {
        val userId = CommonFun.getCurrentUserId()
        Log.i("HabitStatusFixer", "⏳ Starting sync...$userId")
        val db = FirebaseFirestore.getInstance()

        userId?.let { id ->
            db.collection("goals")
                .document(id)
                .collection("Habit")
                .get()
                .addOnSuccessListener { snapshot ->
                    val today = LocalDate.now()

                    for (doc in snapshot.documents) {
                        val goal = doc.toObject(Goal::class.java) ?: continue
                        val progress = goal.progress.toMutableMap()
                        val selectedDays = goal.selectedDays
                        val startDate = runCatching { LocalDate.parse(goal.startDate) }.getOrNull() ?: continue

                        // Find the last recorded completed or missed date
                        val lastRecorded = progress.entries
                            .filter { it.value == 0 || it.value == 1 }
                            .mapNotNull { runCatching { LocalDate.parse(it.key) }.getOrNull() }
                            .maxOrNull() ?: startDate.minusDays(1)

                        var updated = false
                        var current = lastRecorded.plusDays(1)

                        while (!current.isAfter(today.minusDays(1))) {
                            val dayStr = current.toString()
                            val dayOfWeek = current.dayOfWeek.value % 7

                            if (selectedDays.contains(dayOfWeek)) {
                                if (progress.containsKey(dayStr)) {
                                    if (progress[dayStr] == 3) {
                                        progress[dayStr] = 1 // pending -> missed
                                        updated = true
                                    }
                                } else {
                                    progress[dayStr] = 1 // mark as missed
                                    updated = true
                                }
                            }
                            current = current.plusDays(1)
                        }

                        // Mark today as pending if selected and not already filled
                        val todayStr = today.toString()
                        val todayDay = today.dayOfWeek.value % 7
                        if (selectedDays.contains(todayDay)
                            && progress[todayStr]!=0) {
                            progress[todayStr] = 3 // pending
                            updated = true
                        }

                        if (updated) {
                            val sortedProgress = progress.toSortedMap()
                            var totalCompleted = 0
                            var currentStreak = 0
                            var bestStreak = 0
                            var tempStreak = 0

                            val completedRequiredDates = sortedProgress.entries
                                .filter { (dateStr, _) ->
                                    val date = runCatching { LocalDate.parse(dateStr) }.getOrNull()
                                    date != null && selectedDays.contains(date.dayOfWeek.value % 7)
                                }

                            for ((_, status) in completedRequiredDates) {
                                if (status == 0) {
                                    tempStreak++
                                    totalCompleted++
                                    if (tempStreak > bestStreak) bestStreak = tempStreak
                                } else if (status == 1) {
                                    tempStreak = 0
                                }
                            }

                            val todayStatus = progress[todayStr]
                            if (selectedDays.contains(todayDay) && todayStatus == 0) {
                                currentStreak = tempStreak
                            }

                            val totalRequiredDays = generateSequence(startDate) { it.plusDays(1) }
                                .takeWhile { !it.isAfter(today) }
                                .count { selectedDays.contains(it.dayOfWeek.value % 7) }

                            val successRate = if (totalRequiredDays > 0) {
                                (totalCompleted * 100) / totalRequiredDays
                            } else 0

                            db.collection("goals")
                                .document(id)
                                .collection("Habit")
                                .document(doc.id)
                                .update(
                                    mapOf(
                                        "progress" to progress,
                                        "totalCompleted" to totalCompleted,
                                        "currentStreak" to currentStreak,
                                        "bestStreak" to bestStreak,
                                        "successRate" to successRate
                                    )
                                )
                                .addOnSuccessListener {
                                    Log.i("HabitStatusFixer", "✅ Progress + Analytics updated for ${goal.title}")
                                }
                                .addOnFailureListener {
                                    Log.e("HabitStatusFixer", "❌ Failed to update analytics: ${it.message}")
                                }
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("HabitStatusFixer", "❌ Failed to fetch goals: ${it.message}")
                }
        }
    }
}