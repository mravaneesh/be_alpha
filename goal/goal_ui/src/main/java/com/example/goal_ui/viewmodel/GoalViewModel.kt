package com.example.goal_ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goal_domain.model.Goal
import com.example.goal_domain.usecase.GetGoalsUseCase
import com.example.goal_ui.state.GoalState
import com.example.goal_ui.state.HabitAnalyticsState
import com.example.goal_ui.worker.HabitStatusFixer
import com.example.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val getGoalsUseCase: GetGoalsUseCase
) : ViewModel() {

    private val _habitGoals = MutableStateFlow(GoalState())
    val habitGoals: StateFlow<GoalState> = _habitGoals

    private val _trackGoals = MutableStateFlow(GoalState())
    val trackGoals: StateFlow<GoalState> = _trackGoals

    private val _progressUpdate = MutableStateFlow(HabitAnalyticsState.LOADING)
    val progressUpdate: StateFlow<HabitAnalyticsState> = _progressUpdate

     fun loadHabitGoals(userId: String,category:String) {
       getGoalsUseCase(userId,category).onEach{
           when(it){
               is Resource.Loading -> {
                   _habitGoals.value = GoalState(isLoading = true)
               }
               is Resource.Success -> {
                   _habitGoals.value = GoalState(goals = it.data)
               }
               is Resource.Error -> {
                   _habitGoals.value = GoalState(error = it.message)
               }
           }
       }.launchIn(viewModelScope)
    }

    fun loadTrackGoals(userId: String,category:String) {
        getGoalsUseCase(userId,category).onEach{
            when(it){
                is Resource.Loading -> {
                    _trackGoals.value = GoalState(isLoading = true)
                }
                is Resource.Success -> {
                    _trackGoals.value = GoalState(goals = it.data)
                }
                is Resource.Error -> {
                    _trackGoals.value = GoalState(error = it.message)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun updateGoalAnalytics(
        userId: String,
        goal: Goal,
        date: String = LocalDate.now().toString()) {
        viewModelScope.launch {

            val dayOfWeek = LocalDate.parse(date).dayOfWeek.value % 7 // Make Sunday = 0
            val isRequired = goal.selectedDays.contains(dayOfWeek)

            val progress = goal.progress.toMutableMap()
            var currentStreak = goal.currentStreak
            var bestStreak = goal.bestStreak
            var totalCompleted = goal.totalCompleted

            progress[date] = 0
            if (isRequired) {
                currentStreak++
                totalCompleted++
                if (currentStreak > bestStreak) bestStreak = currentStreak
            }

            val totalPossibleDays = calculateTotalRequiredDays(goal.startDate, goal.selectedDays)
            val successRate = if(totalPossibleDays > 0) {
                (totalCompleted * 100) / totalPossibleDays
            } else 0


            val goalRef = FirebaseFirestore.getInstance()
                .collection("goals")
                .document(userId)
                .collection("Habit")
                .document(goal.id)

                goalRef.update(
                    mapOf(
                        "progress" to progress,
                        "currentStreak" to currentStreak,
                        "bestStreak" to bestStreak,
                        "successRate" to successRate,
                        "totalCompleted" to totalCompleted
                    )
                )
                .addOnSuccessListener {
                    Log.i("Firestore", "Goal analytics updated successfully")
                    loadHabitGoals(userId,"Habit")

                    Log.i("Firestore", "${_habitGoals.value.goals}")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error updating analytics", e)
                }
        }
    }
    private fun calculateTotalRequiredDays(startDate: String, selectedDays: List<Int>): Int {
        val start = LocalDate.parse(startDate)
        val today = LocalDate.now()
        var count = 0

        var current = start
        while (!current.isAfter(today)) {
            if (selectedDays.contains(current.dayOfWeek.value % 7)) {
                count++
            }
            current = current.plusDays(1)
        }

        return count
    }

//    fun syncHabitsIfNeeded(context: Context) {
//        HabitStatusFixer.syncMissedAndPendingDays { newState ->
//            _progressUpdate.value = newState
//        }
//    }



}
