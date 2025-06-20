package com.example.bealpha_

import android.app.Application
import com.example.goal_ui.worker.WorkerScheduler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        WorkerScheduler.scheduleHabitCheckWorker(this)
    }
}