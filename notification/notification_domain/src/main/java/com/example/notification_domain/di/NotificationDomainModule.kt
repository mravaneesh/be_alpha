package com.example.notification_domain.di

import com.example.notification_domain.repository.NotificationRepository
import com.example.notification_domain.usecase.GetNotificationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationDomainModule {

    @Provides
    @Singleton
    fun provideGetNotificationUseCase(
        repository: NotificationRepository
    ): GetNotificationUseCase {
        return GetNotificationUseCase(repository)
    }
}