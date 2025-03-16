package com.example.goal_ui.analytics.utils

import java.time.LocalDate

fun LocalDate.toDateListUntil(endDate: LocalDate): List<LocalDate> {
    val dates = mutableListOf<LocalDate>()
    var current = this
    while (!current.isAfter(endDate)) {
        dates.add(current)
        current = current.plusDays(1)
    }
    return dates
}
