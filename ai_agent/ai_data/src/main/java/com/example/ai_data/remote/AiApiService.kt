package com.example.ai_data.remote

import com.example.ai_data.model.DashboardRequestDto
import com.example.ai_data.model.DashboardResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AiApiService {
    @POST("dashboard")
    suspend fun getDashboard(
        @Body request: DashboardRequestDto
    ): DashboardResponseDto
}
