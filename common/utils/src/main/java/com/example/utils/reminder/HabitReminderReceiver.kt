package com.example.utils.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.utils.R

/**
 * Fires at a habit's reminder time: posts the notification, then re-schedules the next occurrence
 * (so the per-habit alarm keeps repeating on its selected days).
 */
class HabitReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra(HabitReminderScheduler.EXTRA_HABIT_ID) ?: return
        val habitName = intent.getStringExtra(HabitReminderScheduler.EXTRA_HABIT_NAME) ?: "your habit"
        val time = intent.getStringExtra(HabitReminderScheduler.EXTRA_TIME)
        val days = intent.getIntArrayExtra(HabitReminderScheduler.EXTRA_DAYS)?.toList()

        ensureChannel(context)
        postNotification(context, habitId, habitName)

        // Re-arm the next occurrence.
        if (time != null && days != null) {
            HabitReminderScheduler.schedule(context, habitId, habitName, time, days)
        }
    }

    private fun postNotification(context: Context, habitId: String, habitName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return // No permission → skip silently.

        val openApp = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val contentIntent = openApp?.let {
            PendingIntent.getActivity(
                context, habitId.hashCode(), it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_reminder)
            .setContentTitle("Time for $habitName")
            .setContentText("Keep your streak alive — mark it done 🔥")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .apply { if (contentIntent != null) setContentIntent(contentIntent) }
            .build()

        NotificationManagerCompat.from(context).notify(habitId.hashCode(), notification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Habit reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Daily reminders to complete your habits"
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "habit_reminder_channel"
    }
}
