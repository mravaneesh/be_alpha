package com.example.bealpha_.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.goal_domain.sync.GoalSynchronizer
import com.example.goal_domain.sync.SyncOutcome
import com.example.goal_domain.sync.SyncScheduler
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager shell around [GoalSyncer]. Deliberately thin — no sync logic lives here, so the
 * algorithm can be tested without a WorkManager harness.
 *
 * Requires BaseApplication to implement Configuration.Provider and supply HiltWorkerFactory,
 * and the default WorkManagerInitializer to be removed from the manifest.
 */
@HiltWorker
class GoalSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncer: GoalSynchronizer,
    private val scheduler: SyncScheduler,
    private val auth: FirebaseAuth,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString(KEY_USER_ID) ?: return Result.success()

        // Work enqueued for a previous session must never run against the current one, or account
        // A's pending goals get written into goals/{B}/… after a logout-and-switch.
        if (userId != auth.currentUser?.uid) return Result.success()

        return when (syncer.syncOnce(userId)) {
            is SyncOutcome.Done -> Result.success()
            // Nothing failed — new local changes arrived mid-pass. Ask for another pass rather
            // than retry(), so we don't burn the backoff curve on a non-failure.
            is SyncOutcome.MoreWork -> { scheduler.request(); Result.success() }
            is SyncOutcome.Retry -> Result.retry()
        }
    }

    companion object {
        const val KEY_USER_ID = "userId"
    }
}
