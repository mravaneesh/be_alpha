package com.example.notification_domain.usecase

import com.example.notification_domain.repository.NotificationRepository
import javax.inject.Inject

class SyncNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(userId: String) = repository.syncNotifications(userId)
}
