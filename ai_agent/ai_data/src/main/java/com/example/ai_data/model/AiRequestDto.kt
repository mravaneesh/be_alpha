package com.example.ai_data.model

data class AiRequestDto(
    val model: String = "llama-3.1-8b-instant",
    val messages: List<MessageDto>,
    val temperature: Double = 0.7
)


