package com.example.social.domain.model

/** A pending friend request addressed to the current user. */
data class FriendRequest(
    val id: String = "",
    val fromUid: String = "",
    val fromName: String = "",
    val fromUsername: String = "",
    val toUid: String = "",
    val status: String = "pending",
    val createdAt: Long = 0L,
)

/**
 * Lightweight, denormalized snapshot of a user used across the social surfaces (friend list, search
 * result, and later the accountability feed / leaderboard). Avoids reading each friend's habits.
 */
data class FriendSummary(
    val uid: String = "",
    val name: String = "",
    val username: String = "",
    val photoUrl: String = "",
    val currentStreak: Int = 0,
    val todayDone: Int = 0,
    val todayTotal: Int = 0,
    val weeklyCompleted: Int = 0,
    val lastWeekCompleted: Int = 0,
    val weeklyConsistency: Int = 0,
    val level: Int = 1,
    val bestStreak: Int = 0,
    val totalCompleted: Int = 0,
)

/** Result of looking up a user to befriend. */
enum class FriendState { NONE, SELF, PENDING, FRIENDS }

data class UserSearchResult(
    val summary: FriendSummary,
    val state: FriendState,
)

/** One leaderboard row (friends + you), ranked by this week's completions. */
data class LeaderboardEntry(
    val summary: FriendSummary,
    val rank: Int,
    val isMe: Boolean,
)

/** An encouragement a friend sent you (in-app only for now). */
data class Nudge(
    val id: String = "",
    val fromUid: String = "",
    val fromName: String = "",
    val type: String = "cheer",
    val createdAt: Long = 0L,
    val seen: Boolean = false,
)
