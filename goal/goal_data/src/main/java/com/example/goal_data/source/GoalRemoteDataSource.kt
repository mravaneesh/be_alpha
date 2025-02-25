package com.example.goal_data.source

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
                .document(userId)  // Retrieve user's goal document
                .collection(category)  // Assuming goals are inside this subcollection
                .get()
                .await()

            snapshot.toObjects(GoalDto::class.java)

        } catch (e: Exception) {
            emptyList() // Return empty list on error
        }
    }
}
