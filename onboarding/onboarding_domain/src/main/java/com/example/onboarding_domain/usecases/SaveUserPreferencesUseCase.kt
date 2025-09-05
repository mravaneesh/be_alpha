package com.example.onboarding_domain.usecases

import com.example.onboarding_domain.model.UserPreferences
import com.example.onboarding_domain.repository.UserPreferencesRepository
import com.example.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SaveUserPreferencesUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    operator fun invoke(prefs: UserPreferences): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            repository.saveUserPreferences(prefs)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown error"))
        }
    }
}