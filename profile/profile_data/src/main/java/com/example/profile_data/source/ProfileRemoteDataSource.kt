package com.example.profile_data.source

import android.util.Log
import com.example.profile_data.model.UserProfileDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileRemoteDataSource@Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getUserProfile(userId: String): UserProfileDto? {
        return try {
            Log.e("ProfileRemoteDataSource", "Fetched data")
            val snapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            snapshot.toObject(UserProfileDto::class.java)
        } catch (e:Exception){
            Log.e("ProfileRemoteDataSource", "Error fetching user profile", e)
            UserProfileDto()
        }
    }
}