package com.example.goal_ui.state

data class GoalCategoryState(
    val isLoading: Boolean = false,
    val categories: List<String> = emptyList(),
    val error: String = ""
)

