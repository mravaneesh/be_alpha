package com.example.goal_ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddGoalViewModel: ViewModel() {
    private val _selectedColor = MutableLiveData<Int>()
    val selectedColor: LiveData<Int> get() = _selectedColor

    private val _trackSelectedColor = MutableLiveData<Int>()
    val trackSelectedColor: LiveData<Int> get() = _trackSelectedColor

    private val _selectedTime = MutableLiveData<String>()
    val selectedTime: LiveData<String> get() = _selectedTime

    fun setColor(color: Int) {
        _selectedColor.value = color
    }
    fun setTrackColor(color: Int) {
        _trackSelectedColor.value = color
    }

    fun setTime(time: String) {
        _selectedTime.value = time
    }
}