package com.example.social.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goal_domain.model.Goal
import com.example.goal_domain.repository.GoalRepository
import com.example.goal_domain.usecase.HabitCompletion
import com.example.social.domain.model.Challenge
import com.example.social.domain.model.ChallengeMember
import com.example.social.domain.model.FriendSummary
import com.example.social.domain.repository.ChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChallengesViewModel @Inject constructor(
    private val repo: ChallengeRepository,
    private val goalRepo: GoalRepository,
) : ViewModel() {

    val myUid: String? get() = repo.currentUid

    val myChallenges: StateFlow<List<Challenge>> =
        repo.observeMyChallenges().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publicChallenges: StateFlow<List<Challenge>> =
        repo.observePublicChallenges().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** The user's habits — used both to pick an existing habit on create and to find a linked habit. */
    val myHabits: StateFlow<List<Goal>> =
        goalRepo.observeGoals("Habit").stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _toast = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toast: SharedFlow<String> = _toast.asSharedFlow()

    /**
     * Create a challenge backed by one or more habits. [newHabitNames] are created fresh;
     * [existingHabitIds] link the user's existing habits (preserving their streak/history). The
     * combined set of habit names is stored on the challenge so members materialize the same habits.
     */
    fun create(
        title: String,
        durationDays: Int,
        isPublic: Boolean,
        invited: List<FriendSummary>,
        newHabitNames: List<String>,
        existingHabitIds: List<String>,
    ) {
        viewModelScope.launch {
            runCatching {
                val existing = myHabits.value.filter { it.id in existingHabitIds }
                val habitNames = (newHabitNames + existing.map { it.title }).map { it.trim() }
                    .filter { it.isNotBlank() }.distinct()
                val id = repo.createChallenge(
                    title = title,
                    icon = "spark",
                    metric = "any",
                    durationDays = durationDays,
                    isPublic = isPublic,
                    habitNames = habitNames,
                    invited = invited.map { ChallengeMember(it.uid, it.name, it.username, it.photoUrl) },
                )
                val uid = repo.currentUid
                if (uid != null && id.isNotBlank()) {
                    existing.forEach { goalRepo.updateGoal(uid, it.copy(challengeId = id)) }
                    newHabitNames.map { it.trim() }.filter { it.isNotBlank() }
                        .forEach { name -> goalRepo.updateGoal(uid, newHabit(name, id)) }
                }
            }.onSuccess {
                _toast.tryEmit("Challenge created")
            }.onFailure {
                _toast.tryEmit("Couldn't create challenge: ${it.message ?: "unknown error"}")
            }
        }
    }

    fun join(id: String) {
        viewModelScope.launch {
            runCatching { repo.joinChallenge(id); ensureHabit(id) }
                .onFailure { _toast.tryEmit("Couldn't join: ${it.message ?: "unknown error"}") }
        }
    }

    /** [deleteHabit] true removes the challenge's linked habits; false keeps them as normal habits. */
    fun leave(id: String, deleteHabit: Boolean) {
        viewModelScope.launch {
            runCatching {
                repo.leaveChallenge(id)
                val uid = repo.currentUid
                val goals = myHabits.value.filter { it.challengeId == id }
                if (uid != null) {
                    goals.forEach { goal ->
                        if (deleteHabit) goalRepo.deleteGoal(uid, "Habit", goal.id)
                        else goalRepo.updateGoal(uid, goal.copy(challengeId = ""))
                    }
                }
            }.onFailure { _toast.tryEmit("Couldn't leave: ${it.message ?: "unknown error"}") }
        }
    }

    fun accept(id: String) {
        viewModelScope.launch {
            runCatching { repo.acceptInvite(id); ensureHabit(id) }
                .onFailure { _toast.tryEmit("Couldn't accept: ${it.message ?: "unknown error"}") }
        }
    }

    fun decline(id: String) {
        viewModelScope.launch {
            runCatching { repo.declineInvite(id) }
                .onFailure { _toast.tryEmit("Couldn't decline: ${it.message ?: "unknown error"}") }
        }
    }

    /** Daily check-in: mark every one of the challenge's habits (scheduled today) complete. Progress syncs automatically. */
    fun markTodayDone(challengeId: String) {
        viewModelScope.launch {
            val uid = repo.currentUid ?: return@launch
            val goals = myHabits.value.filter { it.challengeId == challengeId }
            if (goals.isEmpty()) {
                _toast.tryEmit("No habits linked to this challenge")
                return@launch
            }
            val today = LocalDate.now()
            val todo = goals.filter { HabitCompletion.isScheduledOn(it, today) && !HabitCompletion.isDoneOn(it, today) }
            if (todo.isEmpty()) return@launch
            runCatching { todo.forEach { goalRepo.updateGoal(uid, HabitCompletion.markComplete(it)) } }
                .onSuccess { _toast.tryEmit("Checked in 🎉") }
                .onFailure { _toast.tryEmit("Couldn't check in: ${it.message ?: "unknown error"}") }
        }
    }

    /** Build the challenge's habits for an accepted/joined challenge that don't already exist locally. */
    private suspend fun ensureHabit(challengeId: String) {
        val uid = repo.currentUid ?: return
        val ch = (myChallenges.value + publicChallenges.value).firstOrNull { it.id == challengeId } ?: return
        val have = myHabits.value.filter { it.challengeId == challengeId }.map { it.title.trim().lowercase() }.toSet()
        val names = ch.habitNames.ifEmpty { listOfNotNull(ch.title.ifBlank { null }) }
        names.filter { it.trim().lowercase() !in have }.forEach { name ->
            goalRepo.updateGoal(uid, newHabit(name, challengeId))
        }
    }

    private fun newHabit(title: String, challengeId: String) = Goal(
        id = UUID.randomUUID().toString(),
        category = "Habit",
        title = title.ifBlank { "Challenge habit" },
        selectedDays = listOf(0, 1, 2, 3, 4, 5, 6),
        startDate = LocalDate.now().toString(),
        shared = true,
        challengeId = challengeId,
    )
}
