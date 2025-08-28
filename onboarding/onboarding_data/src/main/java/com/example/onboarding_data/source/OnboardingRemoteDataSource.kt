package com.example.onboarding_data.source

import com.example.onboarding_data.model.UserPreferencesEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class OnboardingRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveUserPreferences(userId: String, preferences: UserPreferencesEntity) {
        firestore.collection("preferences")
            .document(userId)
            .set(preferences)
            .await()
    }

    suspend fun getUserPreferences(userId: String): UserPreferencesEntity? {
        val snapshot = firestore.collection("preferences")
            .document(userId)
            .get()
            .await()

        return snapshot.toObject(UserPreferencesEntity::class.java)
    }
}