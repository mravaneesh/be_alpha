package com.example.social.domain.model

import java.time.LocalDate

/**
 * A group challenge / accountability pod. Stored as a single Firestore doc (members + per-member
 * progress denormalized as maps) so the list, detail and leaderboard all read from one document.
 */
data class Challenge(
    val id: String = "",
    val title: String = "",
    val icon: String = "spark",
    val metric: String = "any", // "any" = any completion that day counts; else a habit category
    val durationDays: Int = 30,
    val startEpochDay: Long = 0L,
    val ownerUid: String = "",
    val isPublic: Boolean = false,                // public = discoverable + joinable by anyone
    val habitNames: List<String> = emptyList(),   // the habits that define this challenge (≥1)
    val memberUids: List<String> = emptyList(),   // accepted — count toward progress/leaderboard
    val invitedUids: List<String> = emptyList(),  // pending invites (not counted until accepted)
    val members: List<ChallengeMember> = emptyList(),
)

/** The habits this challenge is built on; falls back to the title for pre-multi-habit challenges. */
fun Challenge.habits(): List<String> = habitNames.ifEmpty { listOfNotNull(title.ifBlank { null }) }

/** The current user's relationship to a challenge. */
fun Challenge.amMember(uid: String?): Boolean = uid != null && uid in memberUids
fun Challenge.amInvited(uid: String?): Boolean = uid != null && uid in invitedUids && uid !in memberUids
fun Challenge.ownerName(): String = members.firstOrNull { it.uid == ownerUid }?.name?.ifBlank { "A friend" } ?: "A friend"

data class ChallengeMember(
    val uid: String = "",
    val name: String = "",
    val username: String = "",
    val photoUrl: String = "",
    val progress: Int = 0,
    /** Epoch-day of this member's most recent check-in (completing the linked habit). 0 = never. */
    val lastDoneEpochDay: Long = 0L,
)

/** True when [member] checked in (completed the linked habit) today. */
fun ChallengeMember.doneToday(): Boolean = lastDoneEpochDay == LocalDate.now().toEpochDay()

/** 1-based day index into the challenge, clamped to [1, durationDays]. */
fun Challenge.dayIndex(): Int {
    val today = LocalDate.now().toEpochDay()
    return (today - startEpochDay + 1).toInt().coerceIn(1, durationDays.coerceAtLeast(1))
}

fun Challenge.daysLeft(): Int = (durationDays - dayIndex()).coerceAtLeast(0)

fun Challenge.isActive(): Boolean {
    val today = LocalDate.now().toEpochDay()
    return today in startEpochDay until (startEpochDay + durationDays)
}

/** Combined completion across members: avg of each member's day-completions over the days elapsed. */
fun Challenge.groupProgress(): Float {
    val dx = dayIndex()
    if (members.isEmpty() || dx <= 0) return 0f
    val total = members.sumOf { it.progress }
    return (total.toFloat() / (members.size * dx)).coerceIn(0f, 1f)
}

fun Challenge.leaderboard(): List<ChallengeMember> = members.sortedByDescending { it.progress }

fun Challenge.myProgress(uid: String?): Int = members.firstOrNull { it.uid == uid }?.progress ?: 0
