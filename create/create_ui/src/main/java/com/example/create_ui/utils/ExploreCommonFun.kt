package com.example.create_ui.utils

import com.example.create_ui.model.Challenge
import com.example.create_ui.model.SuggestedUser
import com.example.utils.model.User

object ExploreCommonFun {
    fun User.toSuggestedUser(currentUserFollowing: List<String>): SuggestedUser {
        return SuggestedUser(
            id = this.id,
            username = this.username,
            profileImageUrl = this.profileImageUrl,
            isFollowing = currentUserFollowing.contains(this.id),
            name = this.name
        )
    }

    fun getSuggestedChallenges(): List<Challenge> {
        return listOf(
            Challenge(
                id = "75_day_hard",
                title = "75-Day Hard",
                description = "Commit to 75 days of intense physical and mental discipline.",
                bannerUrl = "https://images.unsplash.com/photo-1673872685586-5d06b159f645?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                durationDays = 21
            ),
            Challenge(
                id = "meditation_21",
                title = "21-Day Meditation",
                description = "Find inner peace with daily mindfulness and meditation.",
                bannerUrl = "https://images.unsplash.com/photo-1577344718665-3e7c0c1ecf6b?q=80&w=1169&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                durationDays = 21
            ),
            Challenge(
                id = "healthy_eating",
                title = "Eat Healthy",
                description = "Clean eating for better energy and health.",
                bannerUrl = "https://images.unsplash.com/photo-1490645935967-10de6ba17061?q=80&w=1153&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            Challenge(
                id = "gym_30_days",
                title = "30-Day Gym",
                description = "Hit the gym daily for 30 days. No excuses!",
                bannerUrl = "https://images.unsplash.com/photo-1605296867304-46d5465a13f1?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            Challenge(
                id = "half_marathon",
                title = "Run a Half Marathon",
                description = "Train and complete a half marathon with dedication.",
                bannerUrl = "https://images.unsplash.com/photo-1461897104016-0b3b00cc81ee?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            )
        )
    }
}