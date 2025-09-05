package com.example.onboarding_data.model

data class UserPreferencesEntity(
    val userId: String,
    val gender: String? = null,
    val age: Int? = null,
    val heightCm: Int? = null,
    val weightKg: Float? = null,
    val dietType: List<String>? = null,
    val fitnessGoal: List<String>? = null,
    val habitsTrack: List<String>? = null,
    val workoutStyle: List<String>? = null,
    val workoutTime: String? = null
)