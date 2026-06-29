package com.example.bealpha_.fcm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.bealpha_.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Receives FCM pushes (currently friend "cheer" nudges) and registers this device's token under the
 * signed-in user so the push server can target it. The actual send happens server-side (see
 * /social-push) — the app never holds FCM server credentials.
 */
class ApogeeMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        saveToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "Apogee"
        val body = message.notification?.body ?: message.data["body"] ?: "A friend cheered you on!"
        show(applicationContext, title, body)
    }

    private fun show(context: Context, title: String, body: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Cheers from friends", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "When a friend cheers you on" }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        // Land on the Community tab.
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("bealpha://app/friends")).apply {
            setPackage(context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_nudge_heart)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "nudge_channel"
        private const val NOTIFICATION_ID = 99_010

        /** Persist this device's FCM token under the signed-in user so the server can target it. */
        fun saveToken(token: String) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("tokens").document(token)
                .set(mapOf("token" to token, "platform" to "android", "updatedAt" to System.currentTimeMillis()))
        }
    }
}
