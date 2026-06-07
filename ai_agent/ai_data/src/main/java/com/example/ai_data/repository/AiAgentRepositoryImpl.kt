package com.example.ai_data.repository

import android.util.Log
import com.example.ai_data.model.DashboardRequestDto
import com.example.ai_data.model.UserDto
import com.example.ai_data.remote.AiApiService
import com.example.ai_domain.model.AiSuggestion
import com.example.ai_domain.repository.AiAgentRepository
import com.example.onboarding_domain.model.UserPreferences
import javax.inject.Inject

class AiAgentRepositoryImpl @Inject constructor(
    private val apiService: AiApiService
) : AiAgentRepository {

    private val TAG = "AiAgentRepositoryImpl"

    override suspend fun getPersonalizedSuggestions(userPreferences: UserPreferences): AiSuggestion {
        return try {
            val request = DashboardRequestDto(
                user = UserDto(
                    userId = userPreferences.userId,
                    gender = userPreferences.gender,
                    age = userPreferences.age,
                    heightCm = userPreferences.heightCm,
                    weightKg = userPreferences.weightKg?.toDouble(),
                    dietType = userPreferences.dietType,
                    fitnessGoal = userPreferences.fitnessGoal,
                    workoutStyle = userPreferences.workoutStyle,
                    habitsTrack = userPreferences.habitsTrack,
                    workoutTime = userPreferences.workoutTime
                ),
                goals = emptyList(),
                context = "dashboard"
            )

            Log.i(TAG, "Calling dashboard endpoint for user: ${userPreferences.userId}")
            Log.i(TAG, "Request context: ${request.context}, goals: ${request.goals.size}")
            val response = apiService.getDashboard(request)
            Log.i(TAG, "Dashboard response received. lastNode=${response.lastNode}")
            val output = response.dashboardOutput

            val aiSuggestion = AiSuggestion(
                coachMessage = output?.insight ?: "",
                tipOfTheDay = output?.dailyTip ?: "",
                habitSuggestions = output?.suggestedHabits ?: emptyList(),
                progressFeedback = output?.insight ?: ""
            )

            Log.i(TAG, "Mapped AISuggestion (Domain Model): $aiSuggestion")
            aiSuggestion
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching AI suggestions", e)
            throw e
        }
    }
}
