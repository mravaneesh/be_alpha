package com.example.onboarding_domain.usecases

import com.example.onboarding_domain.model.UserPreferences
import com.example.onboarding_domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SaveUserPreferencesUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke(prefs: UserPreferences) {
        repository.saveUserPreferences(prefs)
    }
}