package com.example.home_data.mapper

import com.example.home_data.model.PostDto
import com.example.home_domain.model.Post

fun PostDto.toDomainPost(): Post {
    return Post(
        id = this.id,
        userId = this.userId,
        userName = this.userName,
        habitId = this.habitId,
        habitTitle = this.habitTitle,
        caption = this.caption,
        likes = this.likes,
        comments = this.comments,
        imageUrls = this.imageUrls,
        createdAt = this.createdAt
    )
}