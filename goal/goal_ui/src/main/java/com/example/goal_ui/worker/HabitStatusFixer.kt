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

    private fun syncMissedAndPendingDays() {
        val userId = CommonFun.getCurrentUserId()
        Log.i("HabitStatusFixer", "⏳ Starting sync...$userId")
        val db = FirebaseFirestore.getInstance()
        userId?.let{
            db.collection("goals")
                .document(it)
                .collection("Habit")
                .get()
                .addOnSuccessListener { snapshot ->
                    val today = LocalDate.now()

                    for(doc in snapshot.documents) {
                        val goal = doc.toObject(Goal::class.java) ?:continue
                        val progress = goal.progress.toMutableMap()
                        val selectedDays = goal.selectedDays
                        val startDate = runCatching { LocalDate.parse(goal.startDate) }.getOrNull() ?: continue
                        val lastRecorded = progress.keys
                            .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
                            .maxOrNull() ?: startDate.minusDays(1)

                        var updated  = false
                        var current = lastRecorded.plusDays(1)

                        while (!current.isAfter(today)) {
                            val dayStr = current.toString()
                            val dayOfWeek = current.dayOfWeek.value % 7

                            when{
                                progress.containsKey(dayStr) ->{
                                    if(progress[dayStr] == 3 && current.isBefore(today)) {
                                        progress[dayStr] =1
                                        updated = true
                                    }
                                }
                                current == today ->{
                                    if(selectedDays.contains(dayOfWeek)) {
                                        progress[dayStr] = 3
                                        updated = true
                                    } else {
                                        progress[dayStr] = 2
                                        updated = true
                                    }
                                }
                                selectedDays.contains(dayOfWeek) -> {
                                    progress[dayStr] = 1
                                    updated = true
                                }
                                else -> {
                                    progress[dayStr] = 2
                                    updated = true
                                }
                            }
                            current = current.plusDays(1)
                        }

                        if(updated) {
                            val sortedProgress = progress.toSortedMap()
                            var totalCompleted = 0
                            var currentStreak = 0
                            var bestStreak = 0
                            var tempStreak = 0

                            Log.i("HabitStatusFixer", "Progress = $sortedProgress")


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
                                    tempStreak = 0 // streak broken
                                }
                            }

                            // Set current streak as the streak ending today (if today is completed)
                            val todayStr = LocalDate.now().toString()
                            val todayStatus = progress[todayStr]
                            if (selectedDays.contains(LocalDate.now().dayOfWeek.value % 7) && todayStatus == 0) {
                                currentStreak = tempStreak
                            }

                            val totalRequiredDays = generateSequence(startDate) { it.plusDays(1) }
                                .takeWhile { !it.isAfter(LocalDate.now()) }
                                .count { selectedDays.contains(it.dayOfWeek.value % 7) }

                            val successRate = if (totalRequiredDays > 0) {
                                (totalCompleted * 100) / totalRequiredDays
                            } else 0

                            // 🔄 Update everything
                            db.collection("goals")
                                .document(it)
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