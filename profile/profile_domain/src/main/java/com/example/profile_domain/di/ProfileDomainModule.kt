package com.example.profile_domain.di

import com.example.profile_domain.repository.ProfileRepository
import com.example.profile_domain.usecase.GetProfileUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object GoalDomainModule {

    @Provides
    fun provideGetGoalsUseCase(repository: ProfileRepository): GetProfileUseCase {
        return GetProfileUseCase(repository)
    }
}