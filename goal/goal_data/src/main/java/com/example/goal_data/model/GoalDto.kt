package com.example.goal_data.model

data class GoalDto (
    val id: String = "",
    val category: String = "",
    val name: String = "",
    val frequency: String = "",
    val userId: String = "" // To associate goal with a user
)