package com.example.bealpha_.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.goal_domain.sync.SyncScheduler
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enqueues one unique background sync pass.
 *
 * KEEP (not REPLACE) so a burst of mutations coalesces into a single pending pass instead of
 * cancelling an upload that is already in flight. The gap that leaves — changes landing *during*
 * a run — is closed by [GoalSyncWorker] returning MoreWork and asking for another pass.
 */
@Singleton
class WorkManagerSyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
) : SyncScheduler {

    override fun request() {
        val userId = auth.currentUser?.uid ?: return

        val request = OneTimeWorkRequestBuilder<GoalSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .setInputData(workDataOf(GoalSyncWorker.KEY_USER_ID to userId))
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(UNIQUE_NAME, ExistingWorkPolicy.KEEP, request)
    }

    private companion object {
        const val UNIQUE_NAME = "goal-sync"
    }
}