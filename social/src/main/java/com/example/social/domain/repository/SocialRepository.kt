package com.example.social.domain.repository

import com.example.social.domain.model.FriendRequest
import com.example.social.domain.model.FriendSummary
import com.example.social.domain.model.LeaderboardEntry
import com.example.social.domain.model.Nudge
import com.example.social.domain.model.UserSearchResult
import kotlinx.coroutines.flow.Flow

/**
 * Friend-graph + accountability operations backed directly by Firestore (no server). Reads are live
 * snapshot Flows; writes are suspend one-shots. All scoped to the currently signed-in user.
 */
interface SocialRepository {

    val currentUid: String?

    /** Friends with their live activity summary (today progress, streak, weekly) for the feed. */
    fun observeFeed(): Flow<List<FriendSummary>>

    /** Friends + you, ranked by this week's completions. */
    fun observeLeaderboard(): Flow<List<LeaderboardEntry>>

    /** Incoming pending friend requests for the current user (live). */
    fun observeIncomingRequests(): Flow<List<FriendRequest>>

    /** Unseen nudges sent to the current user (live) — drives badges. */
    fun observeUnseenNudges(): Flow<List<Nudge>>

    /** Recent nudges (seen + unseen, newest first) for the Notifications screen. */
    fun observeRecentNudges(): Flow<List<Nudge>>

    /** Prefix search over username and name (case-handled), returning up to a handful of matches. */
    suspend fun searchUsers(query: String): List<UserSearchResult>

    /** People to add when the search box is empty — for now, all users (minus yourself), with state. */
    suspend fun suggestUsers(): List<UserSearchResult>
    suspend fun sendFriendRequest(targetUid: String)
    suspend fun acceptRequest(request: FriendRequest)
    suspend fun declineRequest(request: FriendRequest)

    /** Remove an accepted friend (deletes the friend card on both sides). */
    suspend fun removeFriend(friendUid: String)

    /** Send an encouragement nudge to a friend (in-app). */
    suspend fun sendNudge(toUid: String)

    /** Mark all the current user's nudges as seen. */
    suspend fun markNudgesSeen()

    /** Write the current user's denormalized activity summary (called when habits change). */
    suspend fun updateMyActivity(
        todayDone: Int,
        todayTotal: Int,
        currentStreak: Int,
        weeklyCompleted: Int,
        lastWeekCompleted: Int,
        weeklyConsistency: Int,
        level: Int,
        bestStreak: Int,
        totalCompleted: Int,
    )
}
