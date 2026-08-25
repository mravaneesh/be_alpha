package com.example.goal_data.sync

import android.util.Log
import com.example.goal_data.db.GoalDao
import com.example.goal_data.mapper.toDomainGoal
import com.example.goal_data.mapper.toDto
import com.example.goal_data.source.GoalRemoteDataSource
import com.example.goal_domain.sync.GoalSynchronizer
import com.example.goal_domain.sync.SyncOutcome
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import java.io.IOException
import javax.inject.Inject

/**
 * The whole sync algorithm. GoalSyncWorker in :app is only a WorkManager shell around this, so
 * everything worth testing lives here and can be exercised with an in-memory Room database and a
 * fake remote — no WorkManager test harness needed.
 *
 * The two rules that make it correct:
 *
 *  1. Capture the revision you are about to upload (call it N) together with the row, and upload
 *     the state that belongs to N.
 *  2. On success advance syncedRevision to *N*, never to the row's current revision — the user may
 *     have mutated the row while the upload was in flight.
 *
 * Idempotent by construction: the upload is `document(id).set(dto)`, a whole-document overwrite of
 * the desired state. Replaying it changes nothing, so a retry after an ack we never saw is safe,
 * and five offline edits to one habit collapse into a single upload rather than five.
 *
 * No Room transaction is ever held across a network call.
 */
class GoalSyncer @Inject constructor(
    private val dao: GoalDao,
    private val remote: GoalRemoteDataSource,
) : GoalSynchronizer {

    override suspend fun syncOnce(userId: String): SyncOutcome {
        val dirty = dao.getUnsynced()
        if (dirty.isEmpty()) return SyncOutcome.Done

        dirty.forEach { row ->
            // Snapshot the revision alongside the state it describes. If the user taps this habit
            // again while the upload is in flight, revision moves on and the row stays dirty — the
            // next pass picks it up. Advancing to row.revision *after* the call would lose that tap.
            val uploaded = row.revision
            try {
                if (row.pendingDelete) {
                    remote.deleteGoal(userId, row.category, row.id)
                    dao.purgeTombstone(row.id)
                } else {
                    remote.setGoal(userId, row.category, row.toDomainGoal().toDto())
                    dao.markSynced(row.id, uploaded)
                }
            } catch (c: CancellationException) {
                // The worker was cancelled (constraints lost, deadline). Not our failure to classify.
                throw c
            } catch (t: Throwable) {
                if (isRetryable(t)) return SyncOutcome.Retry(t)
                // Permanent: retrying loops forever and blocks every row behind it. Leave the row
                // dirty, log loudly, and let the rest of the queue through.
                Log.e("GoalSync", "permanent failure uploading ${row.id}, skipping", t)
            }
        }

        // Re-read rather than assume: mutations that landed while we were uploading are new work,
        // not a failure, so the worker asks for another pass instead of burning the backoff curve.
        return if (dao.getUnsynced().isEmpty()) SyncOutcome.Done else SyncOutcome.MoreWork
    }

    /**
     * Retryable means "the same request could succeed later": the network or the backend was
     * unavailable. Permanent means the request itself is wrong — bad rules, malformed document —
     * and no amount of backoff fixes it.
     */
    private fun isRetryable(t: Throwable): Boolean = when (t) {
        is FirebaseFirestoreException -> when (t.code) {
            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
            FirebaseFirestoreException.Code.ABORTED,
            FirebaseFirestoreException.Code.INTERNAL,
            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED,
            // The ID token may simply have expired; the next attempt gets a fresh one.
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> true

            else -> false
        }

        is IOException -> true
        else -> false
    }
}
