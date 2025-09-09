package com.example.ai_domain.usecase

import com.example.ai_domain.model.AiSuggestion
import com.example.ai_domain.repository.AiAgentRepository
import com.example.onboarding_domain.model.UserPreferences
import javax.inject.Inject

class GetPersonalizedSuggestionsUseCase @Inject constructor(
    private val repository: AiAgentRepository
) {
    suspend operator fun invoke(userPreferences: UserPreferences): AiSuggestion {
        return repository.getPersonalizedSuggestions(userPreferences)
    }
}