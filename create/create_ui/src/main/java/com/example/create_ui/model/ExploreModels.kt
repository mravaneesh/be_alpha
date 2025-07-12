package com.example.create_ui.model

data class SuggestedUser(
    val id: String,
    val name: String,
    val username: String,
    val profileImageUrl: String,
    val isFollowing: Boolean = false
)

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val bannerUrl: String,
    val durationDays: Int = 30,
    val createdAt: Long = System.currentTimeMillis()
)

data class ExploreGroup(
    val id: String,
    val name: String,
    val members: Int,
    val description: String
)

data class Gym(
    val id: String,
    val name: String,
    val location: String,
    val imageUrl: String
)

