package com.example.goal_ui.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate


class MarkMissedHabitsWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override suspend fun doWork(): Result {
        val uid = auth.currentUser?.uid ?: return Result.failure()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val yesterdayString = yesterday.toString()
        val todayString = today.toString()
        val yesterdayDayOfWeek = yesterday.dayOfWeek.value % 7 // to match 0 = Sunday

        try {
            val goalsSnapshot = db.collection("goals")
                .document(uid)
                .collection("Habit")
                .get()
                .await()

            for (doc in goalsSnapshot.documents) {
                val data = doc.data ?: continue
                val selectedDays = (data["selectedDays"] as? List<*>)?.mapNotNull { (it as? Long)?.toInt() } ?: continue
                val startDate = LocalDate.parse(data["startDate"] as String)
                val progress = ((data["progress"] as? Map<*, *>)?.mapNotNull {
                    val key = it.key as? String
                    val value = (it.value as? Long)?.toInt()
                    if (key != null && value != null) key to value else null
                } ?: emptyList()).toMap().toMutableMap()

                if (yesterday.isBefore(startDate)) continue

                if (selectedDays.contains(yesterdayDayOfWeek)) {
                    val status = progress[yesterdayString]
                    if (status == null || status == 3) {
                        progress[yesterdayString] = 1 // MISSED
                    }
                } else {
                    if (!progress.containsKey(yesterdayString)) {
                        progress[yesterdayString] = 2 // OUT_OF_RANGE
                    }
                }

                // Mark today as PENDING (if applicable)
                val todayDayOfWeek = today.dayOfWeek.value % 7
                if (selectedDays.contains(todayDayOfWeek)) {
                    progress.putIfAbsent(todayString, 3) // PENDING
                }

                db.collection("goals")
                    .document(uid)
                    .collection("Habit")
                    .document(doc.id)
                    .update("progress", progress)
                    .await()
            }

            Log.i("MarkWorker", "✅ Progress update complete")
            return Result.success()
        } catch (e: Exception) {
            Log.e("MarkWorker", "❌ Failed to update progress: ${e.message}")
            return Result.retry()
        }
    }
}

