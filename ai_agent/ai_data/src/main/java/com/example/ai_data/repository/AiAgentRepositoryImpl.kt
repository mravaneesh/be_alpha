package com.example.ai_data.repository

import android.util.Log
import com.example.ai_data.model.AiRequestDto
import com.example.ai_data.model.AiResponseDto
import com.example.ai_data.model.MessageDto
import com.example.ai_data.remote.GroqApiService
import com.example.ai_domain.model.AiSuggestion
import com.example.ai_domain.repository.AiAgentRepository
import com.example.onboarding_domain.model.UserPreferences
import com.google.gson.Gson
import javax.inject.Inject

class AiAgentRepositoryImpl @Inject constructor(
    private val apiService: GroqApiService
) : AiAgentRepository {

    private val TAG = "AiAgentRepositoryImpl"

    override suspend fun getPersonalizedSuggestions(userPreferences: UserPreferences): AiSuggestion {
        Log.i(TAG, "Building AI request for user: ${userPreferences.userId}")

        val userPrefsJson = Gson().toJson(userPreferences)
        val messageContent = """
                You are a personalized fitness and habit coach.
                The user preferences are: $userPrefsJson
                
                🚨 IMPORTANT: 
                - Only respond with a valid JSON object.
                - Do not include explanations, markdown, or text outside the JSON.
                - JSON must strictly follow this schema:
                {
                  "coachMessage": "string",
                  "tipOfTheDay": "string",
                  "habitSuggestions": ["string"],
                  "progressFeedback": "string"
                }
        """.trimIndent()


        val request = AiRequestDto(messages = listOf(MessageDto(content = messageContent)))
        Log.i(TAG, "Request JSON: ${Gson().toJson(request)}")

        return try {
            val response = apiService.getAiSuggestions(request)
            var jsonContent = response.choices.firstOrNull()?.message?.content
                ?: throw Exception("Empty AI response")
            Log.i(TAG, "Raw AI response: $jsonContent")

            jsonContent = extractJsonSafely(jsonContent)
            val aiResponseDto = Gson().fromJson(jsonContent, AiResponseDto::class.java)
            Log.i(TAG, "Parsed AI response DTO: $aiResponseDto")

            val aiSuggestion = AiSuggestion(
                coachMessage = aiResponseDto.coachMessage,
                tipOfTheDay = aiResponseDto.tipOfTheDay,
                habitSuggestions = aiResponseDto.habitSuggestions,
                progressFeedback = aiResponseDto.progressFeedback
            )
            Log.i(TAG, "Mapped AISuggestion (Domain Model): $aiSuggestion")
            aiSuggestion
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching AI suggestions", e)
            throw e
        }
    }

    private fun extractJsonSafely(response: String): String {
        val jsonRegex = "\\{.*\\}".toRegex(RegexOption.DOT_MATCHES_ALL)
        return jsonRegex.find(response)?.value ?: response // fallback: return as-is
    }
}