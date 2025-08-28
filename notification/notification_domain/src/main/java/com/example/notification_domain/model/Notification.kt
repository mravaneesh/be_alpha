package com.example.notification_domain.model

data class Notification(
    val id: String,
    val type: String,
    val title: String,
    val description: String,
    val timestamp: Long
)
