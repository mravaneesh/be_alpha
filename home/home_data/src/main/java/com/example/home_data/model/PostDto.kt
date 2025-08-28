package com.example.home_data.model

import java.util.UUID

data class PostDto(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val userName:String ="",
    val habitId: String = "",
    val habitTitle: String = "",
    val caption: String = "",
    val likes: List<String> = emptyList(),
    val comments: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
