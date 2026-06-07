package com.example.ai_data.model

import com.google.gson.annotations.SerializedName

data class DashboardRequestDto(
    val user: UserDto,
    val goals: List<GoalDto> = emptyList(),
    val context: String? = null
)

data class UserDto(
    val userId: String,
    val gender: String?,
    val age: Int?,
    val heightCm: Int?,
    val weightKg: Double?,
    val dietType: List<String>?,
    val fitnessGoal: List<String>?,
    val workoutStyle: List<String>?,
    val habitsTrack: List<String>?,
    val workoutTime: String?
)

data class GoalDto(
    val id: String,
    val category: String?,
    val title: String?,
    val currentStreak: Int?,
    val successRate: Int?
)

data class DashboardResponseDto(
    @SerializedName("dashboard_output")
    val dashboardOutput: DashboardOutputDto? = null,
    @SerializedName("last_node")
    val lastNode: String? = null
)

data class DashboardOutputDto(
    @SerializedName("daily_tip")
    val dailyTip: String? = null,
    @SerializedName("suggested_habits")
    val suggestedHabits: List<String> = emptyList(),
    val insight: String? = null
)
