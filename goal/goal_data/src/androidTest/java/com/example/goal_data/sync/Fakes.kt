package com.example.goal_data.sync

import com.example.goal_data.model.GoalDto
import com.example.goal_data.source.GoalRemoteDataSource
import com.example.goal_domain.sync.SyncScheduler
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CompletableDeferred

/**
 * A remote we can make misbehave on purpose. Everything the sync design defends against — being
 * offline, a permanent rejection, an upload that stalls while the user keeps tapping — is a field
 * on this class.
 */
class FakeRemote : GoalRemoteDataSource {

    /** What the server currently holds, keyed by goal id. */
    val docs = linkedMapOf<String, GoalDto>()

    /** Every upload in order, so a test can assert *which* revision of a goal was sent. */
    val uploads = mutableListOf<GoalDto>()

    var failGet: Throwable? = null
    var failSet: Throwable? = null
    var failDelete: Throwable? = null

    /** Awaited before each upload completes. Set it to stall an upload mid-flight. */
    var setGate: CompletableDeferred<Unit>? = null

    /** Completed as soon as an upload starts, so a test can act while one is in flight. */
    var setEntered: CompletableDeferred<Unit>? = null

    override suspend fun getGoals(userId: String, category: String): List<GoalDto> {
        failGet?.let { throw it }
        return docs.values.filter { it.category == category }
    }

    override suspend fun setGoal(userId: String, category: String, dto: GoalDto) {
        setEntered?.complete(Unit)
        setGate?.await()
        failSet?.let { throw it }
        uploads += dto
        docs[dto.id] = dto
    }

    override suspend fun deleteGoal(userId: String, category: String, goalId: String) {
        failDelete?.let { throw it }
        docs.remove(goalId)
    }
}

/** Records sync requests instead of touching WorkManager. */
class RecordingScheduler : SyncScheduler {
    var requests = 0
        private set

    override fun request() {
        requests++
    }
}

/** Retryable: the same request could succeed later. */
fun unavailable() = FirebaseFirestoreException(
    "offline", FirebaseFirestoreException.Code.UNAVAILABLE
)

/** Permanent: the request itself is rejected, so retrying it forever would block the queue. */
fun permissionDenied() = FirebaseFirestoreException(
    "denied", FirebaseFirestoreException.Code.PERMISSION_DENIED
)
