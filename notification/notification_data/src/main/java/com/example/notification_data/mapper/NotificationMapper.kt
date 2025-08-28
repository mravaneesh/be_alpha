package com.example.notification_data.mapper

import com.example.goal_domain.model.Goal
import com.example.notification_data.local.NotificationEntity
import com.example.notification_data.model.SocialNotificationDto
import com.example.notification_domain.model.Notification

fun SocialNotificationDto.toEntity() = NotificationEntity(
    id = id,
    type = type,
    title = when(type) {
        "like" -> "Someone liked your post"
        "comment" -> "Someone commented on your post"
        "follow" -> "You have a new follower"
        else -> "Notification"
    },
    description = "from $fromUserId",
    timestamp = timestamp
)

fun NotificationEntity.toDomain() = Notification(
    id = id,
    type = type,
    title = title,
    description = description,
    timestamp = timestamp
)

fun Goal.toReminderEntities(): NotificationEntity? {
    return if(reminder.isNotEmpty()) {
        NotificationEntity(
            id = "reminder_$id",
            type = "reminder",
            title = "Habit Reminder",
            description = "Time for: $title at $reminder",
            timestamp = System.currentTimeMillis()
        )
    } else null
}