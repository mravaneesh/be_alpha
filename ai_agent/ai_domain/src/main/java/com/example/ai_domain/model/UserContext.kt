package com.example.ai_domain.model

import com.example.goal_domain.model.Goal
import com.example.onboarding_domain.model.UserPreferences
import com.example.utils.model.User

data class UserContext(
    val user: User,
    val goals: List<Goal>,
    val preferences: UserPreferences
)