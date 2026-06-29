package com.example.profile_ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Psychology
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goal_domain.model.Goal
import com.example.goal_domain.repository.GoalRepository
import com.example.home_domain.model.Post
import com.example.home_domain.usecase.GetFeedPostUseCase
import com.example.profile_domain.usecase.GetProfileUseCase
import com.example.profile_ui.compose.Achievement
import com.example.profile_ui.compose.ProfileStats
import com.example.profile_ui.state.ProfileState
import com.example.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel@Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val getFeedPostUseCase: GetFeedPostUseCase,
    private val goalRepository: GoalRepository,
): ViewModel() {

    private val _profile = MutableStateFlow(ProfileState(isLoading = true))
    val profile: StateFlow<ProfileState> = _profile

    /** Gamification stats derived live from the offline-first habit cache, so they stay correct
     *  across config changes and update automatically when habits are completed. */
    val stats: StateFlow<ProfileStats> = goalRepository.observeGoals("Habit")
        .map { computeStats(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileStats())

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    // The profile is a one-shot fetch; guard so it loads once per VM lifetime (survives config
    // changes / tab switches). Use [refresh] after an explicit edit.
    private var loadedUserId: String? = null

    /** Idempotent: loads the profile once and warms the habit cache. Safe to call on every view. */
    fun load(userId: String) {
        if (loadedUserId == userId) return
        loadedUserId = userId
        loadProfile(userId)
        viewModelScope.launch { runCatching { goalRepository.refreshGoals(userId, "Habit") } }
    }

    /** Force a re-fetch after the user edits their profile. */
    fun refresh(userId: String) = loadProfile(userId)

    private fun loadProfile(userId: String) {
        getProfileUseCase(userId)
            .onEach { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _profile.value = ProfileState(isLoading = true)
                    }
                    is Resource.Success -> {
                        _profile.value = ProfileState(profile = resource.data)
                    }
                    is Resource.Error -> {
                        _profile.value = ProfileState(error = resource.message)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun loadUserPosts(userId: String) {
        getFeedPostUseCase(userId)
            .onEach { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _posts.value = emptyList()
                    }

                    is Resource.Success -> {
                        _posts.value = resource.data
                            .filter { it.userId == userId }
                    }

                    is Resource.Error -> {
                        _posts.value = emptyList()
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    /** Derive gamification stats from the user's real habits. */
    private fun computeStats(goals: List<Goal>): ProfileStats {
        val bestStreak = goals.maxOfOrNull { it.bestStreak } ?: 0
        val totalCompleted = goals.sumOf { it.totalCompleted }
        val daysActive = goals.flatMap { g -> g.progress.filterValues { it == 0 }.keys }.toSet().size
        val xp = totalCompleted * 10
        val level = 1 + xp / 500
        val xpInLevel = xp % 500
        val title = when {
            level >= 15 -> "Unstoppable"
            level >= 10 -> "Relentless"
            level >= 6 -> "Disciplined"
            level >= 3 -> "Consistent"
            else -> "Beginner"
        }
        val achievements = listOf(
            Achievement("First Win", Icons.Filled.CheckCircle, totalCompleted >= 1),
            Achievement("Week Streak", Icons.Filled.LocalFireDepartment, bestStreak >= 7),
            Achievement("Monthly", Icons.Outlined.CalendarMonth, bestStreak >= 30),
            Achievement("Focused", Icons.Outlined.Psychology, totalCompleted >= 50),
            Achievement("Centurion", Icons.Outlined.EmojiEvents, totalCompleted >= 100),
            Achievement("Legend", Icons.Filled.WorkspacePremium, level >= 10),
        )
        return ProfileStats(
            daysActive = daysActive,
            bestStreak = bestStreak,
            totalCompleted = totalCompleted,
            xp = xp,
            level = level,
            levelTitle = title,
            xpToNext = 500 - xpInLevel,
            levelProgress = xpInLevel / 500f,
            achievements = achievements,
        )
    }

}
