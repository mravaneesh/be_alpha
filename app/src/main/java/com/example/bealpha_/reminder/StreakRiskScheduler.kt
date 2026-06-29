package com.example.bealpha_.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

/**
 * Schedules one daily "your streak is at risk" check in the evening. [StreakRiskReceiver] fires,
 * inspects today's still-incomplete habits with an active streak, notifies if any, and re-arms the
 * next evening. Inexact (allow-while-idle) alarm — no exact-alarm permission needed.
 */
object StreakRiskScheduler {

    private const val HOUR_OF_DAY = 20 // 8 PM
    private const val REQUEST_CODE = 99_001

    fun scheduleDaily(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        runCatching {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTrigger(), pendingIntent(context))
        }.onFailure { Log.e("StreakRisk", "schedule failed", it) }
    }

    private fun nextTrigger(): Long {
        val now = Calendar.getInstance()
        val cal = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, HOUR_OF_DAY)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= now.timeInMillis) cal.add(Calendar.DAY_OF_YEAR, 1)
        return cal.timeInMillis
    }

    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context, REQUEST_CODE, Intent(context, StreakRiskReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
