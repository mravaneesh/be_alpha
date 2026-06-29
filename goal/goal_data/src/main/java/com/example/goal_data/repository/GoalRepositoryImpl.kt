package com.example.goal_data.repository

import android.util.Log
import com.example.goal_data.db.GoalDao
import com.example.goal_data.mapper.toDomainGoal
import com.example.goal_data.mapper.toDto
import com.example.goal_data.mapper.toEntity
import com.example.goal_data.source.GoalRemoteDataSource
import com.example.goal_domain.model.Goal
import com.example.goal_domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Offline-first: reads stream from Room ([observeGoals]); [refreshGoals] best-effort syncs from
 * Firestore into Room (keeps the cache on failure, so it works offline); writes update Room
 * immediately and push to Firestore.
 */
class GoalRepositoryImpl @Inject constructor(
    private val remote: GoalRemoteDataSource,
    private val dao: GoalDao,
) : GoalRepository {

    override fun observeGoals(category: String): Flow<List<Goal>> =
        dao.observe(category).map { list -> list.map { it.toDomainGoal() } }

    override suspend fun refreshGoals(userId: String, category: String) {
        runCatching { remote.getGoals(userId, category) }
            .onSuccess { dtos -> dao.replaceCategory(category, dtos.map { it.toEntity(category) }) }
            .onFailure { Log.w("GoalRepository", "refresh failed, keeping cache: ${it.message}") }
    }

    override suspend fun updateGoal(userId: String, goal: Goal) {
        dao.upsert(goal.toEntity())                       // optimistic local update -> instant UI
        runCatching { remote.setGoal(userId, goal.category, goal.toDto()) }
            .onFailure { Log.w("GoalRepository", "remote update queued/failed: ${it.message}") }
    }

    override suspend fun deleteGoal(userId: String, category: String, goalId: String) {
        dao.deleteById(goalId)
        runCatching { remote.deleteGoal(userId, category, goalId) }
            .onFailure { Log.w("GoalRepository", "remote delete queued/failed: ${it.message}") }
    }
}
