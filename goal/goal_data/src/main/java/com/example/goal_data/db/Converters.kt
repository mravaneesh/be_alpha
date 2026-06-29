package com.example.goal_data.db

import androidx.room.TypeConverter

/** Room converters for the habit list/map fields (no external JSON dependency). */
class Converters {

    @TypeConverter
    fun fromIntList(value: List<Int>): String = value.joinToString(",")

    @TypeConverter
    fun toIntList(value: String): List<Int> =
        if (value.isBlank()) emptyList() else value.split(",").mapNotNull { it.trim().toIntOrNull() }

    @TypeConverter
    fun fromProgress(value: Map<String, Int>): String =
        value.entries.joinToString(";") { "${it.key}:${it.value}" }

    @TypeConverter
    fun toProgress(value: String): Map<String, Int> {
        if (value.isBlank()) return emptyMap()
        return value.split(";").mapNotNull { entry ->
            val idx = entry.lastIndexOf(':')
            if (idx <= 0) return@mapNotNull null
            val key = entry.substring(0, idx)
            val v = entry.substring(idx + 1).toIntOrNull() ?: return@mapNotNull null
            key to v
        }.toMap()
    }
}
