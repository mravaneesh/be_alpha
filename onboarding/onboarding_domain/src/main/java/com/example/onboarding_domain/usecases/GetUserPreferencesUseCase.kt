package com.example.onboarding_domain.usecases

import com.example.onboarding_domain.model.UserPreferences
import com.example.onboarding_domain.repository.UserPreferencesRepository
import javax.inject.Inject

class GetUserPreferencesUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke(userId: String): UserPreferences? {
        return repository.getUserPreferences(userId)
    }
}