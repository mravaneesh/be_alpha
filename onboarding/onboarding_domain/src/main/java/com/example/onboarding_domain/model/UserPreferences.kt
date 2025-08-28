package com.example.onboarding_domain.model


data class UserPreferences(
    val userId: String,
    val age: Int? = null,
    val heightCm: Int? = null,
    val weightKg: Float? = null,
    val dietType: String? = null,       // e.g. "Vegan", "Vegetarian", "Keto"
    val allergies: List<String> = emptyList(),
    val fitnessGoal: String? = null     // e.g. "Weight Loss", "Muscle Gain"
)


