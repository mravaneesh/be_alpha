package com.example.goal_domain.sync

/**
 * Pushes locally-changed goals to the remote. One pass, no scheduling concerns.
 *
 * Declared here rather than in goal_data because :app depends only on goal_domain — the sync
 * worker lives in :app and must be able to name this type. The implementation (GoalSyncer) stays
 * in goal_data next to the DAO it needs.
 */
interface GoalSynchronizer {
    suspend fun syncOnce(userId: String): SyncOutcome
}

/** Result of one sync pass. Small on purpose — the worker maps it straight to a WorkManager Result. */
sealed interface SyncOutcome {

    /** Everything that was dirty is now acknowledged by the remote. */
    data object Done : SyncOutcome

    /**
     * The pass finished, but rows are dirty again — local mutations landed while we were uploading.
     * Request another pass rather than retrying; nothing actually failed.
     */
    data object MoreWork : SyncOutcome

    /** Transient failure (offline, timeout, 5xx). Back off and retry. */
    data class Retry(val cause: Throwable) : SyncOutcome
}
