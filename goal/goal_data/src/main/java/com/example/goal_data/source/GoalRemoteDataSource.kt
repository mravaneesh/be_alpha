package com.example.goal_data.source

import com.example.goal_data.model.GoalDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * The remote goal store. An interface rather than a concrete Firestore class so the repository and
 * the syncer can be tested against a fake that fails, stalls, or returns stale snapshots on demand
 * — the situations the sync design exists to handle, and the ones a real backend will not reproduce
 * on request.
 */
interface GoalRemoteDataSource {

    /** Throws when the server cannot be reached, so callers can keep the local cache. */
    suspend fun getGoals(userId: String, category: String): List<GoalDto>

    /** Whole-document overwrite, so replaying the same upload is a no-op. */
    suspend fun setGoal(userId: String, category: String, dto: GoalDto)

    suspend fun deleteGoal(userId: String, category: String, goalId: String)
}

class FirestoreGoalRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : GoalRemoteDataSource {

    private fun habits(userId: String, category: String) =
        firestore.collection("goals").document(userId).collection(category)

    /**
     * SERVER, not DEFAULT: refresh is the only path by which remote deletes reach this device, and
     * a plain get() silently falls back to the Firestore SDK's own cache when offline. That cache
     * can be empty (fresh install) or partial, and the merge would read the missing rows as
     * "deleted remotely". Room is already the offline cache; forcing SERVER makes offline throw,
     * and the repository keeps what it has.
     */
    override suspend fun getGoals(userId: String, category: String): List<GoalDto> {
        val snapshot = habits(userId, category).get(Source.SERVER).await()
        return snapshot.toObjects(GoalDto::class.java)
    }

    override suspend fun setGoal(userId: String, category: String, dto: GoalDto) {
        habits(userId, category).document(dto.id).set(dto).await()
    }

    override suspend fun deleteGoal(userId: String, category: String, goalId: String) {
        habits(userId, category).document(goalId).delete().await()
    }
}
