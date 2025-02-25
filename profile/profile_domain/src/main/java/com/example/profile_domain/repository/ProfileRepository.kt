package com.example.profile_domain.repository

import com.example.profile_domain.model.UserProfile

interface ProfileRepository {
    suspend fun getUserProfile(userId: String): UserProfile
}