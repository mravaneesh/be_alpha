package com.example.goal_ui.state

import com.example.goal_ui.analytics.model.HabitAnalyticsData

sealed class HabitAnalyticsState {
    data object Loading : HabitAnalyticsState()
    data class Success(val data: HabitAnalyticsData) : HabitAnalyticsState()
    data class Error(val message: String) : HabitAnalyticsState()
}
