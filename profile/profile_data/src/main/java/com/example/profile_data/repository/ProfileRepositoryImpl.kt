package com.example.profile_data.repository

import com.example.profile_data.mapper.toDomainUserProfile
import com.example.profile_data.source.ProfileRemoteDataSource
import com.example.profile_domain.model.UserProfile
import com.example.profile_domain.repository.ProfileRepository
import javax.inject.Inject

class ProfileRepositoryImpl@Inject constructor(
    private val dataSource: ProfileRemoteDataSource
): ProfileRepository {
    override suspend fun getUserProfile(userId: String): UserProfile {
        return dataSource.getUserProfile(userId)?.toDomainUserProfile() ?: UserProfile()
    }
}