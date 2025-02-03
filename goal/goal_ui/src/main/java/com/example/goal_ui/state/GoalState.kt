package com.example.goal_ui.state

import com.example.goal_domain.model.Goal

data class GoalState (
    val isLoading: Boolean = false,
    val goals: List<Goal> = emptyList(),
    val error: String = ""
)