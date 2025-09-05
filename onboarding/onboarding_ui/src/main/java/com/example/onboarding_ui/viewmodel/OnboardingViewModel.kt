package com.example.onboarding_ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onboarding_domain.model.UserPreferences
import com.example.onboarding_domain.usecases.SaveUserPreferencesUseCase
import com.example.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val saveUserPreferencesUseCase: SaveUserPreferencesUseCase,
) : ViewModel() {

    private val _saveState = MutableLiveData<Resource<Unit>>()
    val saveState: LiveData<Resource<Unit>> = _saveState

    private val _selectedGender = MutableLiveData<String>("Select")
    val selectedGender: LiveData<String> = _selectedGender

    private val _selectedBirthday = MutableLiveData<String>("Select")
    val selectedBirthday: LiveData<String> = _selectedBirthday

    fun setSelectedGender(gender: String) {
        _selectedGender.value = gender
    }

    fun setSelectedBirthday(birthday: String) {
        _selectedBirthday.value = birthday
    }

    private val _heightInCm = MutableLiveData<Int>()
    val heightInCm: LiveData<Int> = _heightInCm

    private val _weightInKg = MutableLiveData<Float>()
    val weightInKg: LiveData<Float> = _weightInKg

    private val _age = MutableLiveData<Int>()
    val age: LiveData<Int> = _age

    fun setHeightInCm(height: Int) {
        _heightInCm.value = height
    }

    fun setWeightInKg(weight: Float) {
        _weightInKg.value = weight
    }

    fun setAge(age: Int) {
        _age.value = age
    }

    fun saveUserPreferences(prefs: UserPreferences) {
        viewModelScope.launch {
            saveUserPreferencesUseCase(prefs).collect { result ->
                _saveState.value = result
            }
        }
    }
}