package com.example.notification_data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [NotificationEntity::class], version = 1, exportSchema = false)
abstract class NotificationDatabase: RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}