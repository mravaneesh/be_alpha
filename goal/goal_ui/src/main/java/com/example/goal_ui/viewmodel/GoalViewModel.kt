package com.example.goal_ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goal_domain.usecase.GetGoalsUseCase
import com.example.goal_ui.state.GoalState
import com.example.utils.ProgressDialogUtil
import com.example.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val getGoalsUseCase: GetGoalsUseCase
) : ViewModel() {

    private val _habitGoals = MutableStateFlow(GoalState())
    val habitGoals: StateFlow<GoalState> = _habitGoals

    private val _trackGoals = MutableStateFlow(GoalState())
    val trackGoals: StateFlow<GoalState> = _trackGoals


     fun loadHabitGoals(userId: String,category:String,context: Context) {
       getGoalsUseCase(userId,category).onEach{
           when(it){
               is Resource.Loading -> {
                   ProgressDialogUtil.showProgressDialog(context)
               }
               is Resource.Success -> {
                   ProgressDialogUtil.hideProgressDialog()
                   _habitGoals.value = GoalState(goals = it.data)
               }
               is Resource.Error -> {
                   ProgressDialogUtil.hideProgressDialog()
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
}
