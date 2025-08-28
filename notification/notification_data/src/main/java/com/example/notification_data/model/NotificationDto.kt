package com.example.notification_data.model

data class NotificationDto(
    val id: String = "",
    val type: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Long = 0
)
