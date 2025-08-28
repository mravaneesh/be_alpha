package com.example.onboarding_data.repository

import com.example.onboarding_data.model.UserPreferencesEntity
import com.example.onboarding_data.source.RemoteUserPreferencesDataSource
import com.example.onboarding_domain.model.UserPreferences
import com.example.onboarding_domain.repository.UserPreferencesRepository
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val remote: RemoteUserPreferencesDataSource
) : UserPreferencesRepository {

    override suspend fun saveUserPreferences(prefs: UserPreferences) {
        val entity = UserPreferencesEntity(
            age = prefs.age,
            heightCm = prefs.heightCm,
            weightKg = prefs.weightKg,
            dietType = prefs.dietType,
            allergies = prefs.allergies,
            fitnessGoal = prefs.fitnessGoal
        )
        remote.saveUserPreferences(prefs.userId, entity)
    }

    override suspend fun getUserPreferences(userId: String): UserPreferences? {
        val entity = remote.getUserPreferences(userId) ?: return null
        return UserPreferences(
            userId = userId,
            age = entity.age,
            heightCm = entity.heightCm,
            weightKg = entity.weightKg,
            dietType = entity.dietType,
            allergies = entity.allergies,
            fitnessGoal = entity.fitnessGoal
        )
    }
}
