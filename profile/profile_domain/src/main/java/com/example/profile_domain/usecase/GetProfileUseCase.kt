package com.example.profile_domain.usecase

import com.example.profile_domain.model.UserProfile
import com.example.profile_domain.repository.ProfileRepository
import com.example.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetProfileUseCase@Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(userId: String): Flow<Resource<UserProfile>> = flow {
        emit(Resource.Loading())
        try {
            emit(Resource.Success(data = repository.getUserProfile(userId)))
        } catch (e:Exception) {
            emit(Resource.Error(e.message.toString()))
        }
    }
}