package com.example.ai_domain.model

data class AiSuggestion(
    val coachMessage: String,
    val tipOfTheDay: String,
    val habitSuggestions: List<String>,
    val progressFeedback: String
)