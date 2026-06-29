package com.example.goal_data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Local Room cache row for a habit. Mirrors the domain Goal; list/map fields are stored via [Converters]. */
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val category: String,
    val title: String,
    val description: String,
    val selectedDays: List<Int>,
    val color: Int,
    val reminder: String,
    val startDate: String,
    val progress: Map<String, Int>,
    val currentStreak: Int,
    val bestStreak: Int,
    val totalCompleted: Int,
    val successRate: Int,
    val freezesAvailable: Int = 1,
    val shared: Boolean = true,
    val challengeId: String = "",
)
