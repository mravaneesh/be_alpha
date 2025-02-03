package com.example.goal_domain.di

import com.example.goal_domain.repository.GoalRepository
import com.example.goal_domain.usecase.GetGoalsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object GoalDomainModule {

    @Provides
    fun provideGetGoalsUseCase(repository: GoalRepository): GetGoalsUseCase {
        return GetGoalsUseCase(repository)
    }
}

