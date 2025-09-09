package com.example.ai_data.remote

import com.example.ai_data.model.AiRequestDto
import com.example.ai_data.model.ChatResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface GroqApiService {
    @POST("chat/completions")
    suspend fun getAiSuggestions(
        @Body request: AiRequestDto
    ): ChatResponseDto
}