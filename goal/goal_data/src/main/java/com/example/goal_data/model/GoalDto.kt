package com.example.goal_data.model

import java.time.LocalDate

data class GoalDto (
    val id: String = "",
    val category: String = "",
    val title: String = "",
    val description: String = "",
    val frequency: String = "Daily",
    val selectedDays: Int = 7,
    val color: Int = -1,
    val reminder: String = "",
    val startDate: String = "",
    val progress: Map<String, Int> = mapOf(LocalDate.now().toString() to 2),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompleted: Int = 0,
    val successRate: Int = 0
)