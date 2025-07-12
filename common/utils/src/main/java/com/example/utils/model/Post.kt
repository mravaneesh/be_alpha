package com.example.utils.model

import java.util.UUID

data class Post(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val habitId: String = "",
    val habitTitle: String = "",
    val caption: String = "",
    val likes: Int = 0,
    val comments: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

