package com.example.goal_data.mapper

import com.example.goal_data.model.GoalDto
import com.example.goal_domain.model.Goal

fun GoalDto.toDomainGoal():Goal{
    return Goal(
        id = this.id,
        category = this.category,
        title = this.title,
        description = this.description,
        selectedDays = this.selectedDays,
        color = this.color,
        reminder = this.reminder,
        startDate = this.startDate,
        progress = this.progress,
        currentStreak = this.currentStreak,
        bestStreak = this.bestStreak,
        totalCompleted = this.totalCompleted,
        successRate = this.successRate
    )
}