package com.example.notification_data.model

data class SocialNotificationDto(
    val id: String,
    val type: String,
    val fromUserId: String = "",
    val postId: String? = null,
    val timestamp: Long = 0L
)
