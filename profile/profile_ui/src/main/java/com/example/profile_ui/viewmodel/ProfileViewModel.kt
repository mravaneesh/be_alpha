package com.example.profile_ui.viewmodel

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.home_domain.model.Post
import com.example.home_domain.usecase.GetFeedPostUseCase
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
    private val getProfileUseCase: GetProfileUseCase,
    private val getFeedPostUseCase: GetFeedPostUseCase
): ViewModel() {

    private val _profile = MutableStateFlow(ProfileState())
    val profile: StateFlow<ProfileState> = _profile

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

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

                    is Resource.Error -> TODO()
                }
            }
            .launchIn(viewModelScope)
    }

}
