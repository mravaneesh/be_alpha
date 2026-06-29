package com.example.goal_domain.repository

import com.example.goal_domain.model.Goal
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first habit repository. The UI observes the local cache ([observeGoals]); [refreshGoals]
 * pulls the latest from the network into the cache; writes update the cache immediately and sync
 * remotely.
 */
interface GoalRepository {
    fun observeGoals(category: String): Flow<List<Goal>>
    suspend fun refreshGoals(userId: String, category: String)
    suspend fun updateGoal(userId: String, goal: Goal)
    suspend fun deleteGoal(userId: String, category: String, goalId: String)
}
