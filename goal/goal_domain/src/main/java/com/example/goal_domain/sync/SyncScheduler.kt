package com.example.goal_domain.sync

/**
 * Asks for the pending local changes to be pushed to the remote at some point. Fire-and-forget:
 * callers never wait for it and never learn whether it succeeded — durability comes from the
 * revision columns in Room, not from this call.
 *
 * Lives in the domain layer so [com.example.goal_domain.repository.GoalRepository] implementations
 * stay free of any WorkManager dependency; :app binds the real scheduler.
 */
interface SyncScheduler {
    fun request()
}
