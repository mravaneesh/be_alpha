package com.example.ai_domain.repository

import com.example.ai_domain.model.AiSuggestion
import com.example.onboarding_domain.model.UserPreferences

interface AiAgentRepository {
    suspend fun getPersonalizedSuggestions(userPreferences: UserPreferences): AiSuggestion
}