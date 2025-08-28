package com.example.home_data.repository

import com.example.home_data.mapper.toDomainPost
import com.example.home_data.source.PostRemoteDataSource
import com.example.home_domain.model.Post
import com.example.home_domain.repository.PostRepository
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val dataSource: PostRemoteDataSource
): PostRepository {

    override suspend fun getFeedPosts(currentUserId: String): List<Post> {
        val followingUserIds = dataSource.getFollowingUserIds(currentUserId)
        val allUserIds = listOf(currentUserId) + followingUserIds
        val allPosts = mutableListOf<Post>()
        for(userId in allUserIds){
            val posts = dataSource.getPosts(userId)
            allPosts += posts.map { it.toDomainPost() }
        }
        return allPosts.sortedByDescending { it.createdAt }
    }
}