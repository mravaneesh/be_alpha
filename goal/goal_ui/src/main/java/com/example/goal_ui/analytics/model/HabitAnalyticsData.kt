package com.example.goal_ui.analytics.model

import java.time.LocalDate
import java.time.YearMonth

data class HabitAnalyticsData(
    val habitTitle: String,
    val completionRate: Float,
    val logEntries: List<HabitLogEntry>
)

data class HabitLogEntry(
    val date: String,
    val isCompleted: Boolean
)

data class CalendarDay(
    val date: LocalDate,
    val dayType: DayType,
    var status: DayStatus = DayStatus.OUT_OF_RANGE,
    val isSelectable:Boolean = false,
    val isSelectedDay: Boolean = true
)

enum class DayStatus {
    COMPLETED, MISSED, OUT_OF_RANGE,PENDING
}

enum class DayType { CURRENT, PREVIOUS, NEXT }

data class MonthItem(
    val month: YearMonth,
    val days: List<CalendarDay>
)
