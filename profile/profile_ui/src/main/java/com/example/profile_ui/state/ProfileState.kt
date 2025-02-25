package com.example.profile_ui.state

import com.example.profile_domain.model.UserProfile

data class ProfileState (
    val isLoading: Boolean = false,
    val profile: UserProfile = UserProfile(),
    val error: String = ""
)