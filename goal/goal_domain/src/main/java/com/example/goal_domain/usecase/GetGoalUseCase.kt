package com.example.goal_domain.usecase

import com.example.goal_domain.model.Goal
import com.example.goal_domain.repository.GoalRepository
import com.example.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

/** Streams the locally-cached habits (offline-first) as Resource states. */
class GetGoalsUseCase @Inject constructor(
    private val repository: GoalRepository,
) {
    operator fun invoke(category: String): Flow<Resource<List<Goal>>> =
        repository.observeGoals(category)
            .map<List<Goal>, Resource<List<Goal>>> { Resource.Success(it) }
            .onStart { emit(Resource.Loading()) }
            .catch { emit(Resource.Error(it.message ?: "Unable to load habits")) }
}

/** Pulls the latest habits from the network into the local cache (best-effort). */
class RefreshGoalsUseCase @Inject constructor(
    private val repository: GoalRepository,
) {
    suspend operator fun invoke(userId: String, category: String) =
        repository.refreshGoals(userId, category)
}
