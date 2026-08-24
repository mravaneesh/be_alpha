package com.example.goal_data.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.goal_data.db.GoalDao
import com.example.goal_data.db.GoalDatabase
import com.example.goal_data.mapper.toDomainGoal
import com.example.goal_data.mapper.toDto
import com.example.goal_data.mapper.toEntity
import com.example.goal_data.source.GoalRemoteDataSource
import com.example.goal_domain.model.Goal
import com.example.goal_domain.repository.GoalRepository
import com.example.goal_domain.usecase.HabitCompletion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

/**
 * Offline-first: Room is the local source of truth. Reads stream from Room ([observeGoals]);
 * [refreshGoals] best-effort pulls Firestore into Room (keeps the cache on failure, so it works
 * offline).
 *
 * Writes are *commands*, never whole-object replacements: callers pass a goal id plus the minimum
 * the operation needs, and the repository re-reads the latest row before applying the change. Every
 * read-modify-write runs inside a single Room transaction (see [mutate]), so two concurrent writers
 * — a user tap and the background rollover, say — can never each compute from the same snapshot and
 * overwrite one another.
 */
class GoalRepositoryImpl @Inject constructor(
    private val remote: GoalRemoteDataSource,
    private val dao: GoalDao,
    private val db: GoalDatabase,
) : GoalRepository {

    override fun observeGoals(category: String): Flow<List<Goal>> =
        dao.observe(category).map { list -> list.map { it.toDomainGoal() } }

    override suspend fun refreshGoals(userId: String, category: String) {
        runCatching { remote.getGoals(userId, category) }
            .onSuccess { dtos -> dao.replaceCategory(category, dtos.map { it.toEntity(category) }) }
            .onFailure { Log.w("GoalRepository", "refresh failed, keeping cache: ${it.message}") }
    }

    /**
     * The single local-mutation boundary. Reads the freshest row, applies a pure domain transform,
     * and persists — all in one transaction, so no other writer can interleave between the read and
     * the write. Returns the persisted goal, or null when the goal is gone or [transform] reported
     * no change (transforms return null for a no-op, matching [HabitCompletion.rollover]).
     */
    private suspend fun mutate(goalId: String, transform: (Goal) -> Goal?): Goal? =
        db.withTransaction {
            val current = dao.getGoalById(goalId)?.toDomainGoal()
                ?: return@withTransaction null
            val updated = transform(current)
                ?: return@withTransaction null
            dao.upsert(updated.toEntity())
            updated
        }

    override suspend fun completeGoal(userId: String, goalId: String, date: LocalDate) {
        val updated = mutate(goalId) { HabitCompletion.markComplete(it, date) } ?: return
        pushBestEffort(userId, updated)
    }

    override suspend fun undoCompletion(userId: String, goalId: String, date: LocalDate) {
        val updated = mutate(goalId) { HabitCompletion.markIncomplete(it, date) } ?: return
        pushBestEffort(userId, updated)
    }

    /**
     * Bring every habit up to date for [today]. Ids are collected first and each goal gets its own
     * short transaction: one transaction spanning every goal would hold the writer connection for
     * the whole multi-month recompute and stall user taps behind it. A goal deleted mid-pass is
     * skipped ([mutate] returns null); one created mid-pass is picked up on the next cache emission.
     */
    override suspend fun rolloverGoals(userId: String, today: LocalDate) {
        val ids = dao.observe("Habit").first().map { it.id }
        ids.forEach { id ->
            val updated = mutate(id) { HabitCompletion.rollover(it, today) } ?: return@forEach
            pushBestEffort(userId, updated)
        }
    }

    /** Link a habit to a challenge, or unlink it with an empty [challengeId]. */
    override suspend fun setChallengeLink(userId: String, goalId: String, challengeId: String) {
        val updated = mutate(goalId) { current ->
            current.copy(challengeId = challengeId).takeIf { it != current }
        } ?: return
        pushBestEffort(userId, updated)
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
        val updated = mutate(goalId) { current ->
            current.copy(
                title = title,
                description = description,
                selectedDays = selectedDays,
                color = color,
                reminder = reminder,
            ).takeIf { it != current }
        } ?: return
        pushBestEffort(userId, updated)
    }

    /**
     * A brand-new goal carries a fresh id, so there is no existing row to go stale against — this is
     * the one write that legitimately takes a whole [Goal].
     */
    override suspend fun createGoal(userId: String, goal: Goal) {
        dao.upsert(goal.toEntity())
        pushBestEffort(userId, goal)
    }

    override suspend fun deleteGoal(userId: String, category: String, goalId: String) {
        dao.deleteById(goalId)
        runCatching { remote.deleteGoal(userId, category, goalId) }
            .onFailure { Log.w("GoalRepository", "remote delete queued/failed: ${it.message}") }
    }

    /**
     * INTERIM: pushes the committed row to Firestore outside the transaction, best-effort. This is
     * deliberately not part of the local critical path, but it is also not durable — a failure (or
     * process death right after the commit) is only logged and never retried. It stands in until the
     * pending-sync flag + WorkManager drain replaces it.
     */
    private suspend fun pushBestEffort(userId: String, goal: Goal) {
        runCatching { remote.setGoal(userId, goal.category, goal.toDto()) }
            .onFailure { Log.w("GoalRepository", "remote push failed: ${it.message}") }
    }
}
