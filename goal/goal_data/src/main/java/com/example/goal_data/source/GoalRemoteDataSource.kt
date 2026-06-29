package com.example.goal_data.source

import com.example.goal_data.model.GoalDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GoalRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun habits(userId: String, category: String) =
        firestore.collection("goals").document(userId).collection(category)

    /** Throws on failure so the repository can keep the existing Room cache when offline. */
    suspend fun getGoals(userId: String, category: String): List<GoalDto> {
        val snapshot = habits(userId, category).get().await()
        return snapshot.toObjects(GoalDto::class.java)
    }

    suspend fun setGoal(userId: String, category: String, dto: GoalDto) {
        habits(userId, category).document(dto.id).set(dto).await()
    }

    suspend fun deleteGoal(userId: String, category: String, goalId: String) {
        habits(userId, category).document(goalId).delete().await()
    }
}
