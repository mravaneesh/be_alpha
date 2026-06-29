package com.example.goal_domain.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Use cases ([com.example.goal_domain.usecase.GetGoalsUseCase],
 * [com.example.goal_domain.usecase.RefreshGoalsUseCase]) are provided via their @Inject
 * constructors, so no explicit bindings are needed here.
 */
@InstallIn(SingletonComponent::class)
@Module
object GoalDomainModule
