package com.example.profile_data.mapper

import com.example.profile_data.model.UserProfileDto
import com.example.profile_domain.model.UserProfile

fun UserProfileDto.toDomainUserProfile(): UserProfile {
    return UserProfile(
        userId = this.id,
        name = this.name,
        userName = this.username,
        bio = this.bio,
        birthdate = this.birthdate,
        gender = this.gender,
        profileImageUrl = this.profileImageUrl,
        followers = this.followers,
        following = this.following,
        posts = this.posts,
    )
}