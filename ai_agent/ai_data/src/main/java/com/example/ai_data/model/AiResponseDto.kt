package com.example.ai_data.model

data class AiResponseDto(
    val coachMessage: String,
    val tipOfTheDay: String,
    val habitSuggestions: List<String>,
    val progressFeedback: String
)
