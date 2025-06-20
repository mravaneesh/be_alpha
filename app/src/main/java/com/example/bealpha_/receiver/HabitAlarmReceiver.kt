package com.example.bealpha_.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.goal_ui.worker.MarkMissedHabitsWorker

class HabitAlarmReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context,intent: Intent?) {
        Log.i("HabitAlarmReceiver", "Alarm Triggered!")
        Toast.makeText(context, "Receiver Triggered!", Toast.LENGTH_SHORT).show()
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<MarkMissedHabitsWorker>().build()
        )
    }
}