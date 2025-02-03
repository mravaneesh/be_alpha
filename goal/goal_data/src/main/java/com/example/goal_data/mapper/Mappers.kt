package com.example.goal_data.mapper

import com.example.goal_data.model.GoalDto
import com.example.goal_domain.model.Goal

fun GoalDto.toDomainGoal():Goal{
    return Goal(
        id = this.id,
        category = this.category,
        name = this.name,
        frequency = this.frequency
    )
}