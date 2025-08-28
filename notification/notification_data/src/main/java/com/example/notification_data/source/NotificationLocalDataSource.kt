package com.example.notification_data.source

import com.example.notification_data.local.NotificationDao
import com.example.notification_data.local.NotificationEntity
import javax.inject.Inject

class NotificationLocalDataSource @Inject constructor(
    private val dao: NotificationDao
) {
    suspend fun getAll() = dao.getAllNotifications()
    suspend fun savaAll(notifications: List<NotificationEntity>) = dao.insertNotification(notifications)
}