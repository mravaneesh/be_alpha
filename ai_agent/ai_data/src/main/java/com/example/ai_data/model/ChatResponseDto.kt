package com.example.ai_data.model

data class ChatResponseDto(
    val choices: List<ChoiceDto>
)

data class ChoiceDto(
    val message: MessageDto
)
