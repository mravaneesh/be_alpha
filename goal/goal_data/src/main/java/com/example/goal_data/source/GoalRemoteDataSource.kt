package com.example.goal_data.source

import com.example.goal_data.model.GoalDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GoalRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun habits(userId: String, category: String) =
        firestore.collection("goals").document(userId).collection(category)

    /**
     * SERVER, not DEFAULT: refresh is the only path by which remote deletes reach this device, and
     * a plain get() silently falls back to the Firestore SDK's own cache when offline. That cache
     * can be empty (fresh install) or partial, and the merge would read the missing rows as
     * "deleted remotely". Room is already the offline cache; forcing SERVER makes offline throw,
     * and the repository keeps what it has.
     */
    suspend fun getGoals(userId: String, category: String): List<GoalDto> {
        val snapshot = habits(userId, category).get(Source.SERVER).await()
        return snapshot.toObjects(GoalDto::class.java)
    }

    suspend fun setGoal(userId: String, category: String, dto: GoalDto) {
        habits(userId, category).document(dto.id).set(dto).await()
    }

    suspend fun deleteGoal(userId: String, category: String, goalId: String) {
        habits(userId, category).document(goalId).delete().await()
    }
}
