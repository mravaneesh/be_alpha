package com.example.notification_ui.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.notification_domain.model.Notification
import com.example.notification_domain.usecase.GetNotificationUseCase
import com.example.notification_ui.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class HabitReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
    private val getNotificationUseCase: GetNotificationUseCase
): CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val all = getNotificationUseCase().first()
         val reminders: List<Notification> = all.filter { it.type == "reminder" }

        reminders.forEach {
            sendNotification(it)
        }

        return Result.success()
    }

    private fun sendNotification(notification: Notification) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "habit_reminder_channel"

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,"Reminders",NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notif = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(notification.title)
            .setContentText(notification.description)
            .setSmallIcon(com.example.ui.R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(notification.id.hashCode(), notif)
    }


}