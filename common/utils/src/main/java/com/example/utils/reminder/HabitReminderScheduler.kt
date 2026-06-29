package com.example.utils.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Schedules per-habit daily reminders via [AlarmManager]. Each habit gets one pending alarm at
 * its reminder time on its next selected day; [HabitReminderReceiver] posts the notification and
 * re-schedules the following occurrence. Uses inexact (allow-while-idle) alarms so no exact-alarm
 * permission is required.
 *
 * Day index convention matches the app: 0 = Sunday … 6 = Saturday.
 */
object HabitReminderScheduler {

    const val EXTRA_HABIT_ID = "habitId"
    const val EXTRA_HABIT_NAME = "habitName"
    const val EXTRA_TIME = "time"
    const val EXTRA_DAYS = "days"

    /** Schedule (or reschedule) the reminder for a habit. Blank time / no days → cancels it. */
    fun schedule(context: Context, habitId: String, habitName: String, time: String, selectedDays: List<Int>) {
        if (time.isBlank() || selectedDays.isEmpty() || habitId.isBlank()) {
            cancel(context, habitId)
            return
        }
        val triggerAt = nextTriggerMillis(time, selectedDays) ?: return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent(context, habitId, habitName, time, selectedDays))
        } catch (e: Exception) {
            Log.e("HabitReminder", "Failed to schedule reminder for $habitName", e)
        }
    }

    fun cancel(context: Context, habitId: String) {
        if (habitId.isBlank()) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            context, habitId.hashCode(), baseIntent(context).putExtra(EXTRA_HABIT_ID, habitId),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        if (pi != null) {
            alarmManager.cancel(pi)
            pi.cancel()
        }
    }

    private fun pendingIntent(context: Context, habitId: String, habitName: String, time: String, days: List<Int>): PendingIntent {
        val intent = baseIntent(context).apply {
            putExtra(EXTRA_HABIT_ID, habitId)
            putExtra(EXTRA_HABIT_NAME, habitName)
            putExtra(EXTRA_TIME, time)
            putExtra(EXTRA_DAYS, days.toIntArray())
        }
        return PendingIntent.getBroadcast(
            context, habitId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun baseIntent(context: Context) =
        Intent(context, HabitReminderReceiver::class.java)

    /** Next time (epoch millis) matching a selected day at [time]; null if the time can't be parsed. */
    fun nextTriggerMillis(time: String, selectedDays: List<Int>, from: Calendar = Calendar.getInstance()): Long? {
        val (hour, minute) = parseTime(time) ?: return null
        for (offset in 0..7) {
            val cal = from.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, offset)
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val appDay = cal.get(Calendar.DAY_OF_WEEK) - 1 // Calendar SUNDAY=1 -> app 0=Sunday
            if (appDay in selectedDays && cal.timeInMillis > from.timeInMillis) {
                return cal.timeInMillis
            }
        }
        return null
    }

    private fun parseTime(time: String): Pair<Int, Int>? = runCatching {
        val date = SimpleDateFormat("hh:mm a", Locale.getDefault()).parse(time.trim()) ?: return null
        val cal = Calendar.getInstance().apply { this.time = date }
        cal.get(Calendar.HOUR_OF_DAY) to cal.get(Calendar.MINUTE)
    }.getOrNull()
}
