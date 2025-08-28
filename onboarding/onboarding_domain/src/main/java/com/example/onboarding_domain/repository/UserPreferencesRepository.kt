package com.example.onboarding_domain.repository

import com.example.onboarding_domain.model.UserPreferences

interface UserPreferencesRepository {
    suspend fun saveUserPreferences(prefs: UserPreferences)
    suspend fun getUserPreferences(userId: String): UserPreferences?
}