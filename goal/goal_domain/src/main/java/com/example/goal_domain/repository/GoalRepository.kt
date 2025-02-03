package com.example.goal_domain.repository

import com.example.goal_domain.model.Goal

interface GoalRepository {
    suspend fun getGoals(userId: String,category: String):List<Goal>
}