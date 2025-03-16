package com.example.goal_ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goal_domain.model.Goal
import com.example.goal_domain.usecase.GetGoalsUseCase
import com.example.goal_ui.analytics.model.CalendarDay
import com.example.goal_ui.state.GoalState
import com.example.utils.ProgressDialogUtil
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

     fun loadHabitGoals(userId: String,category:String) {
       getGoalsUseCase(userId,category).onEach{
           when(it){
               is Resource.Loading -> {
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

    fun loadTrackGoals(userId: String,category:String,context: Context) {
        getGoalsUseCase(userId,category).onEach{
            when(it){
                is Resource.Loading -> {
                    ProgressDialogUtil.showProgressDialog(context)
                }
                is Resource.Success -> {
                    ProgressDialogUtil.hideProgressDialog()
                    _trackGoals.value = GoalState(goals = it.data)
                }
                is Resource.Error -> {
                    ProgressDialogUtil.hideProgressDialog()
                    _trackGoals.value = GoalState(error = it.message)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun updateGoalAnalytics(userId: String, goal: Goal, isCompleted: Boolean) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val updatedAnalytics = goal.progress.toMutableMap()
            var currentStreak = goal.currentStreak
            var bestStreak = goal.bestStreak
            var score = goal.successRate
            var totalCompleted = goal.totalCompleted

            if (!isCompleted) {
                updatedAnalytics[today] = 3
                currentStreak = 0
                totalCompleted--
                score--
            } else {
                updatedAnalytics[today] = 1
                currentStreak++
                if (currentStreak > bestStreak) {
                    bestStreak = currentStreak
                }
                totalCompleted++
                score++
            }

            val goalRef = FirebaseFirestore.getInstance()
                .collection("goals")
                .document(userId)
                .collection("Habit")
                .document(goal.id)

                goalRef.update(
                    mapOf(
                        "progress" to updatedAnalytics,
                        "currentStreak" to currentStreak,
                        "bestStreak" to bestStreak,
                        "successRate" to score,
                        "totalCompleted" to totalCompleted
                    )
                )
                .addOnSuccessListener {
                    Log.d("Firestore", "Goal analytics updated successfully")
                    loadHabitGoals(userId,"Habit")

                    Log.d("Firestore", "${_habitGoals.value.goals}")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error updating analytics", e)
                }
        }
    }
}
