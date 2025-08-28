package com.example.home_domain.repository

import com.example.home_domain.model.Post

interface PostRepository {
    suspend fun getFeedPosts(currentUserId: String): List<Post>
}