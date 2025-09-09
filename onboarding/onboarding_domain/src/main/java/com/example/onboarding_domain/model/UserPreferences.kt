package com.example.onboarding_domain.model

data class UserPreferences(
    val userId: String = "",
    val gender: String? = null,
    val age: Int? = null,
    val heightCm: Int? = null,
    val weightKg: Float? = null,
    val dietType: List<String> = emptyList(),
    val fitnessGoal: List<String> = emptyList(),
    val workoutStyle: List<String> = emptyList(),
    val habitsTrack: List<String> = emptyList(),
    val workoutTime: String? = null
)


