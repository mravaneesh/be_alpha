package com.example.onboarding_data.repository

import com.example.onboarding_data.model.UserPreferencesEntity
import com.example.onboarding_data.source.OnboardingRemoteDataSource
import com.example.onboarding_domain.model.UserPreferences
import com.example.onboarding_domain.repository.UserPreferencesRepository
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val remote: OnboardingRemoteDataSource
) : UserPreferencesRepository {

    override suspend fun saveUserPreferences(prefs: UserPreferences) {
        val entity = UserPreferencesEntity(
            age = prefs.age,
            gender = prefs.gender,
            heightCm = prefs.heightCm,
            weightKg = prefs.weightKg,
            dietType = prefs.dietType,
            fitnessGoal = prefs.fitnessGoal,
            workoutStyle = prefs.workoutStyle,
            habitsTrack = prefs.habitsTrack,
            workoutTime = prefs.workoutTime,
            userId = prefs.userId
        )
        remote.saveUserPreferences(prefs.userId, entity)
    }

    override suspend fun getUserPreferences(userId: String): UserPreferences? {
        val entity = remote.getUserPreferences(userId) ?: return null
        return UserPreferences(
            userId = userId,
            gender = entity.gender,
            age = entity.age,
            heightCm = entity.heightCm,
            weightKg = entity.weightKg,
            dietType = entity.dietType,
            fitnessGoal = entity.fitnessGoal,
            habitsTrack = entity.habitsTrack,
            workoutStyle = entity.workoutStyle,
            workoutTime = entity.workoutTime
        )
    }
}
