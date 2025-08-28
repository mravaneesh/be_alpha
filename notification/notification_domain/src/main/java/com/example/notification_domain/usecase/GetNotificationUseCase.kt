package com.example.notification_domain.usecase

import com.example.notification_domain.model.Notification
import com.example.notification_domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow

class GetNotificationUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke() : Flow<List<Notification>> {
        return repository.getNotifications()
    }
}