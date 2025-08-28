package com.example.onboarding_domain.di

import com.example.onboarding_domain.repository.UserPreferencesRepository
import com.example.onboarding_domain.usecases.GetUserPreferencesUseCase
import com.example.onboarding_domain.usecases.SaveUserPreferencesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object OnboardingDomainModule {

    @Provides
    fun provideSaveUserPreferencesUseCase(
        repo: UserPreferencesRepository
    ) = SaveUserPreferencesUseCase(repo)

    @Provides
    fun provideGetUserPreferencesUseCase(
        repo: UserPreferencesRepository
    ) = GetUserPreferencesUseCase(repo)
}