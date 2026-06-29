package com.example.bealpha_.reminder

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
import com.example.bealpha_.R
import com.example.goal_domain.repository.GoalRepository
import com.example.goal_domain.usecase.HabitCompletion
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Evening streak-saver. Checks habits scheduled today that are still incomplete but carry an active
 * streak; if any, nudges the user to finish them before midnight. Re-arms itself for tomorrow.
 */
@AndroidEntryPoint
class StreakRiskReceiver : BroadcastReceiver() {

    @Inject lateinit var goalRepository: GoalRepository

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val goals = runCatching { goalRepository.observeGoals("Habit").first() }.getOrDefault(emptyList())
                val today = LocalDate.now()
                val todayIdx = today.dayOfWeek.value % 7
                val atRisk = goals.filter {
                    it.selectedDays.contains(todayIdx) &&
                        !HabitCompletion.isDoneOn(it, today) &&
                        it.currentStreak > 0
                }
                if (atRisk.isNotEmpty()) {
                    notify(context, atRisk.size, atRisk.maxOf { it.currentStreak })
                }
            } finally {
                StreakRiskScheduler.scheduleDaily(context) // arm tomorrow's check
                pending.finish()
            }
        }
    }

    private fun notify(context: Context, count: Int, topStreak: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Streak reminders", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Evening nudge when a streak is about to break" }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val openApp = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val contentIntent = openApp?.let {
            PendingIntent.getActivity(context, 99_001, it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val habitWord = if (count == 1) "habit" else "habits"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_w_fire)
            .setContentTitle("Don't break your streak! 🔥")
            .setContentText("$count $habitWord left today — keep your $topStreak-day streak alive.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .apply { if (contentIntent != null) setContentIntent(contentIntent) }
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "streak_risk_channel"
        private const val NOTIFICATION_ID = 99_002
    }
}
