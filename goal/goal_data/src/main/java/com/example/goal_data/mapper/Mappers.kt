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
    shared = shared,
    challengeId = challengeId,
)
fun GoalEntity.withDomain(goal: Goal): GoalEntity = copy(
    category = goal.category,
    title = goal.title,
    description = goal.description,
    selectedDays = goal.selectedDays,
    color = goal.color,
    reminder = goal.reminder,
    startDate = goal.startDate,
    progress = goal.progress,
    currentStreak = goal.currentStreak,
    bestStreak = goal.bestStreak,
    totalCompleted = goal.totalCompleted,
    successRate = goal.successRate,
    shared = goal.shared,
    challengeId = goal.challengeId,
)

/**
 * Copies remote fields onto an existing row. Leaves id/category (the row's identity, and how it was
 * looked up) and the sync columns alone — the caller decides what the row now owes the server.
 */
fun GoalEntity.withDto(dto: GoalDto): GoalEntity = copy(
    title = dto.title,
    description = dto.description,
    selectedDays = dto.selectedDays,
    color = dto.color,
    reminder = dto.reminder,
    startDate = dto.startDate,
    progress = dto.progress,
    currentStreak = dto.currentStreak,
    bestStreak = dto.bestStreak,
    totalCompleted = dto.totalCompleted,
    successRate = dto.successRate,
    shared = dto.shared,
    challengeId = dto.challengeId,
)

fun Goal.toNewEntity(): GoalEntity = GoalEntity(
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
    shared = shared,
    challengeId = challengeId,
    revision = 1,
    syncedRevision = 0,
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
    shared = shared,
    challengeId = challengeId,
    revision = 1,
    syncedRevision = 1
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
    shared = shared,
    challengeId = challengeId,
)
