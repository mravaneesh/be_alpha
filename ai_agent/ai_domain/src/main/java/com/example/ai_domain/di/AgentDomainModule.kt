package com.example.ai_domain.di

import com.example.ai_domain.repository.AiAgentRepository
import com.example.ai_domain.usecase.GetPersonalizedSuggestionsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AgentDomainModule {

    @Provides
    @Singleton
    fun provideGetPersonalizedSuggestionsUseCase(
        repository: AiAgentRepository
    ): GetPersonalizedSuggestionsUseCase {
        return GetPersonalizedSuggestionsUseCase(repository)
    }
}