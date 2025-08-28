package com.example.notification_data.source

import com.example.notification_data.model.HabitReminderDto
import com.example.notification_data.model.SocialNotificationDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRemoteSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getSocialNotifications(userId: String): List<SocialNotificationDto> {
        val snapshot = firestore.collection("notifications")
            .document(userId)
            .collection("userNotifications")
            .orderBy("timestamp")
            .get()
            .await()

        return snapshot.documents.mapNotNull {
            it.toObject(SocialNotificationDto::class.java)
        }
    }
}