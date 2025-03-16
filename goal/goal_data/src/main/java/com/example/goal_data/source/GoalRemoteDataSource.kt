package com.example.goal_data.source

import android.util.Log
import com.example.goal_data.model.GoalDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GoalRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getGoals(userId: String,category: String): List<GoalDto> {
        return try {
            val snapshot = firestore.collection("goals")
                .document(userId)
                .collection("Habit")
                .get()
                .await()
            val goals = snapshot.toObjects(GoalDto::class.java)
            Log.d("GoalDataSource", "Goals successfully retrieved: $goals")
            goals
        } catch (e: Exception) {
            Log.e("GoalDataSource", "Error retrieving goals: ${e.message}")
            emptyList()
        }
    }
}
