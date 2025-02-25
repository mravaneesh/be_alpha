package com.example.profile_ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.profile_domain.model.UserProfile

class EditProfileViewModel : ViewModel() {
    private val _editUser = MutableLiveData<UserProfile>()
    val editUser: LiveData<UserProfile> = _editUser

    private val _selectedGender = MutableLiveData<String>()
    val selectedGender: LiveData<String> = _selectedGender

    private val _selectedBirthday = MutableLiveData<String>()
    val selectedBirthday: LiveData<String> = _selectedBirthday

    fun setUserProfile(userProfile: UserProfile) {
        _editUser.value = userProfile
        _selectedGender.value = userProfile.gender
        _selectedBirthday.value = userProfile.birthdate
    }

    fun setSelectedGender(gender: String) {
        _selectedGender.value = gender
    }

    fun setSelectedBirthday(birthday: String) {
        _selectedBirthday.value = birthday
    }
}
