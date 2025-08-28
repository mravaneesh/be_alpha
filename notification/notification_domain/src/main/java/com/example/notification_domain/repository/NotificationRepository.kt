package com.example.notification_domain.repository

import com.example.notification_domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun getNotifications(): Flow<List<Notification>>
    suspend fun syncNotifications(userId: String)
}