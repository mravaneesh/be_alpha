package com.example.social.domain.repository

import com.example.social.domain.model.Challenge
import com.example.social.domain.model.ChallengeMember
import kotlinx.coroutines.flow.Flow

/** Group challenges backed by a single Firestore doc per challenge (Firestore-only, no server). */
interface ChallengeRepository {

    val currentUid: String?

    /** Challenges the current user is a member of OR invited to (live). */
    fun observeMyChallenges(): Flow<List<Challenge>>

    /** Public, joinable challenges for the Browse tab (global discovery). */
    fun observePublicChallenges(): Flow<List<Challenge>>

    /** Create a challenge with the current user as owner; [invited] are added as pending invites. */
    suspend fun createChallenge(
        title: String,
        icon: String,
        metric: String,
        durationDays: Int,
        isPublic: Boolean,
        habitNames: List<String>,
        invited: List<ChallengeMember>,
    ): String

    /** Join a public challenge directly (self-service, no invite). */
    suspend fun joinChallenge(challengeId: String)

    /** Accept a pending invite: move the current user from invited → accepted member. */
    suspend fun acceptInvite(challengeId: String)

    /** Decline a pending invite (remove the current user from invitedUids). */
    suspend fun declineInvite(challengeId: String)

    /** Leave a challenge the current user has joined. */
    suspend fun leaveChallenge(challengeId: String)

    /** One-shot read of the user's currently-running challenges (for the progress roll-up). */
    suspend fun myActiveChallenges(): List<Challenge>

    /** Write the current user's day-completion count and last check-in day for a challenge. */
    suspend fun updateMyProgress(challengeId: String, progress: Int, lastDoneEpochDay: Long)
}
