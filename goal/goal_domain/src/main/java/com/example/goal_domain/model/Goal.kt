package com.example.goal_domain.model

data class Goal(
    val id: String = "",
    val category: String = "",
    val title: String = "",
    val description: String = "",
    val frequency: String = "Daily",
    val selectedDays:Int = 7,
    val color: Int = -1,
    val reminder: String = "",
    val target: String = "",
    val unit: String = ""
)

