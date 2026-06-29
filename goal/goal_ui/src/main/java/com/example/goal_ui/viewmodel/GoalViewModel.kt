package com.example.goal_ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goal_domain.model.Goal
import com.example.goal_domain.repository.GoalRepository
import com.example.goal_domain.usecase.GetGoalsUseCase
import com.example.goal_domain.usecase.HabitCompletion
import com.example.goal_domain.usecase.RefreshGoalsUseCase
import com.example.goal_ui.state.GoalState
import com.example.goal_ui.state.HabitAnalyticsState
import com.example.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val getGoalsUseCase: GetGoalsUseCase,
    private val refreshGoalsUseCase: RefreshGoalsUseCase,
    private val repository: GoalRepository,
) : ViewModel() {

    private val _habitGoals = MutableStateFlow(GoalState())
    val habitGoals: StateFlow<GoalState> = _habitGoals

    private val _trackGoals = MutableStateFlow(GoalState())
    val trackGoals: StateFlow<GoalState> = _trackGoals

    private val _progressUpdate = MutableStateFlow(HabitAnalyticsState.LOADING)
    val progressUpdate: StateFlow<HabitAnalyticsState> = _progressUpdate

    /** Habit id to scroll to + highlight on the Habits screen (set when a widget habit is tapped). */
    private val _focusHabitId = MutableStateFlow<String?>(null)
    val focusHabitId: StateFlow<String?> = _focusHabitId
    fun focusHabit(id: String?) { _focusHabitId.value = id }

    private var habitObserveJob: Job? = null
    private var trackObserveJob: Job? = null

    /** Offline-first: stream the local cache immediately, refresh from network in the background. */
    fun loadHabitGoals(userId: String, category: String) {
        viewModelScope.launch { runCatching { refreshGoalsUseCase(userId, category) } }
        habitObserveJob?.cancel()
        habitObserveJob = getGoalsUseCase(category).onEach { result ->
            _habitGoals.value = result.toState()
        }.launchIn(viewModelScope)
    }

    fun loadTrackGoals(userId: String, category: String) {
        viewModelScope.launch { runCatching { refreshGoalsUseCase(userId, category) } }
        trackObserveJob?.cancel()
        trackObserveJob = getGoalsUseCase(category).onEach { result ->
            _trackGoals.value = result.toState()
        }.launchIn(viewModelScope)
    }

    private fun Resource<List<Goal>>.toState(): GoalState = when (this) {
        is Resource.Loading -> GoalState(isLoading = true)
        is Resource.Success -> GoalState(goals = data)
        is Resource.Error -> GoalState(error = message)
    }

    /** Mark a habit complete for [date] — updates the local cache instantly and syncs remotely. */
    fun updateGoalAnalytics(userId: String, goal: Goal, date: String = LocalDate.now().toString()) {
        viewModelScope.launch {
            val updated = HabitCompletion.markComplete(goal, LocalDate.parse(date))
            runCatching { repository.updateGoal(userId, updated) }
                .onFailure { Log.e("GoalViewModel", "updateGoal failed", it) }
        }
    }

    /** Undo an accidental completion for today — reverses the streak/total changes. */
    fun undoGoalAnalytics(userId: String, goal: Goal, date: String = LocalDate.now().toString()) {
        viewModelScope.launch {
            val updated = HabitCompletion.markIncomplete(goal, LocalDate.parse(date))
            runCatching { repository.updateGoal(userId, updated) }
                .onFailure { Log.e("GoalViewModel", "undoGoal failed", it) }
        }
    }

    /** Delete a habit — removes from the local cache immediately and syncs remotely. */
    fun deleteHabit(userId: String, goal: Goal) {
        viewModelScope.launch {
            runCatching { repository.deleteGoal(userId, goal.category.ifBlank { "Habit" }, goal.id) }
                .onFailure { Log.e("GoalViewModel", "deleteGoal failed", it) }
        }
    }
}
