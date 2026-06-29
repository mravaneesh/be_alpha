package com.example.goal_data.mapper

import com.example.goal_data.db.GoalEntity
import com.example.goal_data.model.GoalDto
import com.example.goal_domain.model.Goal

fun GoalDto.toDomainGoal(): Goal = Goal(
    id = id,
    category = category,
    title = title,
    description = description,
    selectedDays = selectedDays,
    color = color,
    reminder = reminder,
    startDate = startDate,
    progress = progress,
    currentStreak = currentStreak,
    bestStreak = bestStreak,
    totalCompleted = totalCompleted,
    successRate = successRate,
    freezesAvailable = freezesAvailable,
    shared = shared,
    challengeId = challengeId,
)

fun GoalDto.toEntity(category: String): GoalEntity = GoalEntity(
    id = id,
    category = category.ifBlank { this.category },
    title = title,
    description = description,
    selectedDays = selectedDays,
    color = color,
    reminder = reminder,
    startDate = startDate,
    progress = progress,
    currentStreak = currentStreak,
    bestStreak = bestStreak,
    totalCompleted = totalCompleted,
    successRate = successRate,
    freezesAvailable = freezesAvailable,
    shared = shared,
    challengeId = challengeId,
)

fun GoalEntity.toDomainGoal(): Goal = Goal(
    id = id,
    category = category,
    title = title,
    description = description,
    selectedDays = selectedDays,
    color = color,
    reminder = reminder,
    startDate = startDate,
    progress = progress,
    currentStreak = currentStreak,
    bestStreak = bestStreak,
    totalCompleted = totalCompleted,
    successRate = successRate,
    freezesAvailable = freezesAvailable,
    shared = shared,
    challengeId = challengeId,
)

fun Goal.toDto(): GoalDto = GoalDto(
    id = id,
    category = category,
    title = title,
    description = description,
    selectedDays = selectedDays,
    color = color,
    reminder = reminder,
    startDate = startDate,
    progress = progress,
    currentStreak = currentStreak,
    bestStreak = bestStreak,
    totalCompleted = totalCompleted,
    successRate = successRate,
    freezesAvailable = freezesAvailable,
    shared = shared,
    challengeId = challengeId,
)

fun Goal.toEntity(): GoalEntity = GoalEntity(
    id = id,
    category = category,
    title = title,
    description = description,
    selectedDays = selectedDays,
    color = color,
    reminder = reminder,
    startDate = startDate,
    progress = progress,
    currentStreak = currentStreak,
    bestStreak = bestStreak,
    totalCompleted = totalCompleted,
    successRate = successRate,
    freezesAvailable = freezesAvailable,
    shared = shared,
    challengeId = challengeId,
)
