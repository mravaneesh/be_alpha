package com.example.onboarding_data.model

data class UserPreferencesEntity(
    val age: Int? = null,
    val heightCm: Int? = null,
    val weightKg: Float? = null,
    val dietType: String? = null,
    val allergies: List<String> = emptyList(),
    val fitnessGoal: String? = null
)