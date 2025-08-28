package com.example.home_data.source

import android.util.Log
import com.example.home_data.model.PostDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PostRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getFollowingUserIds(userId: String): List<String> {
        val snapshot = firestore.collection("users")
            .document(userId)
            .get()
            .await()

        return snapshot.get("following") as? List<String> ?: emptyList()
    }

    suspend fun getPosts(userId: String): List<PostDto> {
        val snapshot = firestore.collection("posts")
            .document(userId)
            .collection("userPosts")
            .get()
            .await()

        Log.i("PostRemoteDataSource","getPosts: $snapshot")
        return snapshot.documents.mapNotNull {
            it.toObject(PostDto::class.java)?.copy(id = it.id)
        }
    }
}