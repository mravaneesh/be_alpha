package com.example.bealpha_

import android.app.Application
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.example.bealpha_.widget.ApogeeWidgetUpdater
import com.example.goal_domain.model.Goal
import com.example.goal_domain.repository.GoalRepository
import com.example.goal_domain.usecase.HabitCompletion
import com.example.social.domain.repository.ChallengeRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltAndroidApp
class BaseApplication : Application() {

    @Inject lateinit var goalRepository: GoalRepository
    @Inject lateinit var challengeRepository: ChallengeRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        TransferNetworkLossHandler.getInstance(applicationContext)
        syncDerivedState()
    }

    /**
     * Single source of truth → derived surfaces. Whenever the habit cache changes (from the app OR
     * a widget tap) we (a) re-render any placed widgets and (b) sync the user's per-challenge
     * progress to Firestore (habit data is otherwise private — only challenge-linked habits are
     * shared, with challenge members). A lightweight signature dedupes redundant emissions.
     */
    private fun syncDerivedState() {
        appScope.launch {
            var lastSig: String? = null
            goalRepository.observeGoals("Habit").collect { goals ->
                val sig = signature(goals)
                if (sig == lastSig) return@collect
                val first = lastSig == null
                lastSig = sig
                android.util.Log.d("WidgetSync", "habits changed (n=${goals.size}, first=$first) -> updating widgets")

                // Update on a separate coroutine — calling updateAll() inline inside this Room-flow
                // collector doesn't re-render the widget (the collection coroutine ties it up).
                if (!first) ApogeeWidgetUpdater.refresh(this@BaseApplication)
                runCatching { syncChallengeProgress(goals) }
            }
        }
    }

    /**
     * Recompute the user's day-completion count + last check-in day for each active challenge from
     * its linked habits and write it (idempotent). A challenge can have several habits (all sharing
     * its challengeId); a day counts only when EVERY habit scheduled that day is completed — that is
     * the daily check-in.
     */
    private suspend fun syncChallengeProgress(allGoals: List<Goal>) {
        val me = challengeRepository.currentUid ?: return
        val today = LocalDate.now()
        challengeRepository.myActiveChallenges().forEach { ch ->
            val goals = allGoals.filter { it.challengeId == ch.id }
            if (goals.isEmpty()) return@forEach
            val start = LocalDate.ofEpochDay(ch.startEpochDay)
            val end = minOf(today, start.plusDays((ch.durationDays - 1).toLong()))
            var count = 0
            var lastDone = 0L
            var d = start
            while (!d.isAfter(end)) {
                val scheduled = goals.filter { HabitCompletion.isScheduledOn(it, d) }
                if (scheduled.isNotEmpty() && scheduled.all { HabitCompletion.isDoneOn(it, d) }) {
                    count++
                    lastDone = d.toEpochDay()
                }
                d = d.plusDays(1)
            }
            val mine = ch.members.firstOrNull { it.uid == me }
            if (mine == null || count != mine.progress || lastDone != mine.lastDoneEpochDay) {
                challengeRepository.updateMyProgress(ch.id, count, lastDone)
            }
        }
    }

    private fun signature(goals: List<Goal>): String {
        val today = LocalDate.now().toString()
        return goals.joinToString("|") { "${it.id}:${it.progress[today] ?: 3}:${it.currentStreak}:${it.freezesAvailable}" }
    }
}