package com.example.goal_data.model

data class GoalDto (
    val id: String = "",
    val category: String = "",
    val title: String = "",
    val description: String = "",
    val frequency: String = "Daily",
    val selectedDays:Int = 7,
    val color: Long =0xFFFFFFFF,
    val reminder: String = "",
)