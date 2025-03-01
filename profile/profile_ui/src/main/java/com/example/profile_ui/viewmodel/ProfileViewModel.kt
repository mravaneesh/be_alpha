package com.example.profile_ui.viewmodel

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.profile_domain.usecase.GetProfileUseCase
import com.example.profile_ui.databinding.FragmentProfileBinding
import com.example.profile_ui.state.ProfileState
import com.example.utils.ProgressDialogUtil
import com.example.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel@Inject constructor(
    private val getProfileUseCase: GetProfileUseCase
): ViewModel() {

    private val _profile = MutableStateFlow(ProfileState())
    val profile: StateFlow<ProfileState> = _profile

    fun loadProfile(userId: String) {
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

}
