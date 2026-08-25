package com.example.goal_data.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.goal_data.db.GoalDao
import com.example.goal_data.db.GoalDatabase
import com.example.goal_data.mapper.toDomainGoal
import com.example.goal_data.mapper.toEntity
import com.example.goal_data.mapper.toNewEntity
import com.example.goal_data.mapper.withDomain
import com.example.goal_data.mapper.withDto
import com.example.goal_data.source.GoalRemoteDataSource
import com.example.goal_domain.model.Goal
import com.example.goal_domain.repository.GoalRepository
import com.example.goal_domain.sync.SyncScheduler
import com.example.goal_domain.usecase.HabitCompletion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

/**
 * Offline-first: Room is the local source of truth and the only thing a write ever touches. Reads
 * stream from Room ([observeGoals]); [refreshGoals] merges a remote snapshot back in.
 *
 * Writes are *commands*, never whole-object replacements: callers pass a goal id plus the minimum
 * the operation needs, and the repository re-reads the latest row before applying the change. Every
 * read-modify-write runs inside a single Room transaction (see [mutate]), so two concurrent writers
 * — a user tap and the background rollover, say — can never each compute from the same snapshot and
 * overwrite one another.
 *
 * Nothing here talks to Firestore on the write path. A local write bumps the row's `revision` past
 * its `syncedRevision` and asks [SyncScheduler] for a background pass; that pass is what uploads.
 * This is what makes a completion survive being made offline, or the process dying a second later:
 * the debt is recorded in the database, not in a coroutine.
 */
class GoalRepositoryImpl @Inject constructor(
    private val remote: GoalRemoteDataSource,
    private val dao: GoalDao,
    private val db: GoalDatabase,
    private val scheduler: SyncScheduler,
) : GoalRepository {

    override fun observeGoals(category: String): Flow<List<Goal>> =
        dao.observe(category).map { list -> list.map { it.toDomainGoal() } }

    /**
     * Merge a remote snapshot into the cache. The rule is one line: **the remote is authoritative
     * only for rows we do not owe it anything on.** A row whose `revision` has moved past its
     * `syncedRevision` holds a local change that has not been uploaded, so the snapshot predates it
     * and must not win — the previous clear-and-reinsert lost exactly those rows.
     *
     * On failure the cache is left untouched, so this is safe to call offline.
     */
    override suspend fun refreshGoals(userId: String, category: String) {
        val dtos = runCatching { remote.getGoals(userId, category) }
            .onFailure { Log.w("GoalRepository", "refresh failed, keeping cache: ${it.message}") }
            .getOrNull() ?: return

        db.withTransaction {
            val local = dao.getByCategory(category).associateBy { it.id }
            val remoteIds = dtos.mapTo(mutableSetOf()) { it.id }

            dtos.forEach { dto ->
                val row = local[dto.id]
                when {
                    // Not seen here before: arrives clean, it is exactly what the server holds.
                    row == null -> dao.upsert(dto.toEntity(category))
                    // Deleted here, remote not yet told. Do not resurrect it.
                    row.pendingDelete -> Unit
                    // Edited here, not yet uploaded. Our copy is newer than this snapshot.
                    row.revision != row.syncedRevision -> Unit
                    // Clean: take the remote state, and mark it settled at the row's *current*
                    // revision. Never reset revision to 1 — an upload still in flight for revision
                    // N would ack afterwards and advance syncedRevision past it, stranding the row
                    // permanently dirty.
                    else -> dao.upsert(row.withDto(dto).copy(syncedRevision = row.revision))
                }
            }

            local.values.forEach { row ->
                if (row.id in remoteIds) return@forEach
                when {
                    // We asked for it gone and the server agrees. The tombstone has done its job.
                    row.pendingDelete -> dao.purgeTombstone(row.id)
                    // Created here, never uploaded — of course the server has not heard of it.
                    row.syncedRevision == 0L -> Unit
                    // Deleted elsewhere, but edited here. Keeping the edit resurrects a habit the
                    // user can delete again in one tap; dropping it silently eats a completion.
                    row.revision != row.syncedRevision -> Unit
                    // Clean and gone from the server: deleted on another device.
                    else -> dao.deleteById(row.id)
                }
            }
        }
    }

    /**
     * The single local-mutation boundary. Reads the freshest row, applies a pure domain transform,
     * and persists — all in one transaction, so no other writer can interleave between the read and
     * the write. Returns the persisted goal, or null when the goal is gone or [transform] reported
     * no change (transforms return null for a no-op, matching [HabitCompletion.rollover]).
     */
    private suspend fun mutate(goalId: String, transform: (Goal) -> Goal?): Goal? =
        db.withTransaction {
            val row = dao.getGoalById(goalId)
                ?: return@withTransaction null
            val updated = transform(row.toDomainGoal())
                ?: return@withTransaction null
            // The revision bump lives here, not inside withDomain(): it is the line that makes the
            // row owe the server something, and it belongs in the same transaction as the write.
            dao.upsert(row.withDomain(updated).copy(revision = row.revision + 1))
            updated
        }

    override suspend fun completeGoal(userId: String, goalId: String, date: LocalDate) {
        mutate(goalId) { HabitCompletion.markComplete(it, date) } ?: return
        scheduler.request()
    }

    override suspend fun undoCompletion(userId: String, goalId: String, date: LocalDate) {
        mutate(goalId) { HabitCompletion.markIncomplete(it, date) } ?: return
        scheduler.request()
    }

    /**
     * Bring every habit up to date for [today]. Ids are collected first and each goal gets its own
     * short transaction: one transaction spanning every goal would hold the writer connection for
     * the whole multi-month recompute and stall user taps behind it. A goal deleted mid-pass is
     * skipped ([mutate] returns null); one created mid-pass is picked up on the next cache emission.
     *
     * One scheduler request for the whole pass, after the loop — the rollover touches every habit
     * and there is no reason to ask for a sync per goal.
     */
    override suspend fun rolloverGoals(userId: String, today: LocalDate) {
        val ids = dao.observe("Habit").first().map { it.id }
        var changed = false
        ids.forEach { id ->
            if (mutate(id) { HabitCompletion.rollover(it, today) } != null) changed = true
        }
        if (changed) scheduler.request()
    }

    /** Link a habit to a challenge, or unlink it with an empty [challengeId]. */
    override suspend fun setChallengeLink(userId: String, goalId: String, challengeId: String) {
        mutate(goalId) { current ->
            current.copy(challengeId = challengeId).takeIf { it != current }
        } ?: return
        scheduler.request()
    }

    override suspend fun updateGoalDetails(
        userId: String,
        goalId: String,
        title: String,
        description: String,
        selectedDays: List<Int>,
        color: Int,
        reminder: String,
    ) {
        mutate(goalId) { current ->
            current.copy(
                title = title,
                description = description,
                selectedDays = selectedDays,
                color = color,
                reminder = reminder,
            ).takeIf { it != current }
        } ?: return
        scheduler.request()
    }

    /**
     * A brand-new goal carries a fresh id, so there is no existing row to go stale against — this is
     * the one write that legitimately takes a whole [Goal].
     */
    override suspend fun createGoal(userId: String, goal: Goal) {
        dao.upsert(goal.toNewEntity())
        scheduler.request()
    }

    /**
     * Soft delete. The row stays as a tombstone — hidden from reads, still in the sync queue — until
     * the remote has been told. A hard delete here would leave no record that the server still holds
     * the goal, so dying before the network call would resurrect it on the next refresh.
     */
    override suspend fun deleteGoal(userId: String, category: String, goalId: String) {
        dao.markPendingDelete(goalId)
        scheduler.request()
    }
}
