package com.example.goal_data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [GoalEntity::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class GoalDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
}
