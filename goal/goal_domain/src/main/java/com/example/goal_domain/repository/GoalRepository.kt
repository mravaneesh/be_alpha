package com.example.goal_domain.repository

import com.example.goal_domain.model.Goal
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Offline-first habit repository. The UI observes the local cache ([observeGoals]); [refreshGoals]
 * pulls the latest from the network into the cache; writes update the cache immediately and sync
 * remotely.
 */
interface GoalRepository {
    fun observeGoals(category: String): Flow<List<Goal>>

    suspend fun refreshGoals(
        userId: String,
        category: String
    )

    suspend fun completeGoal(
        userId: String,
        goalId: String,
        date: LocalDate
    )

    suspend fun undoCompletion(
        userId: String,
        goalId: String,
        date: LocalDate
    )

    suspend fun rolloverGoals(
        userId: String,
        today: LocalDate
    )

    suspend fun setChallengeLink(
        userId: String,
        goalId: String,
        challengeId: String
    )

    suspend fun updateGoalDetails(
        userId: String,
        goalId: String,
        title: String,
        description: String,
        selectedDays: List<Int>,
        color: Int,
        reminder: String
    )

    suspend fun createGoal(
        userId: String,
        goal: Goal
    )

    suspend fun deleteGoal(
        userId: String,
        category: String,
        goalId: String
    )
}