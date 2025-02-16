package com.example.goal_data.repository

import com.example.goal_data.mapper.toDomainGoal
import com.example.goal_data.source.GoalRemoteDataSource
import com.example.goal_domain.model.Goal
import com.example.goal_domain.repository.GoalRepository
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor
    (private val dataSource: GoalRemoteDataSource)
    : GoalRepository {

    override suspend fun getGoals(userId: String,category: String): List<Goal> {
        return dataSource.getGoals(userId,category).map { it.toDomainGoal() }
    }

}
