package com.example.home_ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.utils.Prefs
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.designsystem.theme.PactTheme
import com.example.goal_domain.model.Goal
import com.example.goal_domain.repository.GoalRepository
import com.example.home_ui.R
import com.example.home_ui.compose.DashboardScreen
import com.example.home_ui.compose.HabitSummary
import com.example.utils.CommonFun
import com.example.utils.CommonFun.getUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Home tab. Observes the offline-first habit cache (the same Room source the Habits tab and widget
 * use) so completing a habit anywhere reflects here instantly; a background refresh keeps it synced.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    @Inject lateinit var goalRepository: GoalRepository

    private var greeting by mutableStateOf("")
    private var initial by mutableStateOf("")
    private var completed by mutableStateOf(0)
    private var total by mutableStateOf(0)
    private var streakDays by mutableStateOf(0)
    private var habits by mutableStateOf<List<HabitSummary>>(emptyList())
    private var weekly by mutableStateOf(List(7) { 0f })
    private var hasAnyGoals by mutableStateOf(true)
    private var isLoading by mutableStateOf(true)
    private var error by mutableStateOf<String?>(null)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PactTheme {
                    val ctx = LocalContext.current
                    var runHomeTour by remember { mutableStateOf(!Prefs.isScreenTourSeen(ctx, "home")) }
                    DashboardScreen(
                        runTour = runHomeTour,
                        onTourFinished = { Prefs.setScreenTourSeen(ctx, "home", true); runHomeTour = false },
                        greeting = greeting,
                        userInitial = initial,
                        completed = completed,
                        total = total,
                        streakDays = streakDays,
                        habits = habits,
                        weekly = weekly,
                        weeklyPercent = weekly.map { (it * 100).toInt() }.average().toInt().coerceIn(0, 100),
                        hasAnyGoals = hasAnyGoals,
                        isLoading = isLoading,
                        error = error,
                        onNotifications = {
                            findNavController().navigate(R.id.action_homeFragment_to_notificationFragment)
                        },
                        onRetry = ::refresh,
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadGreeting()
        // Live, offline-first: any habit change (this tab, Habits tab, or the widget) flows here.
        goalRepository.observeGoals("Habit")
            .onEach { goals ->
                applyGoals(goals)
                hasAnyGoals = goals.isNotEmpty()
                isLoading = false
                error = null
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val uid = CommonFun.getCurrentUserId()
        if (uid == null) {
            error = "You're signed out. Sign in to see your day."
            isLoading = false
            return
        }
        lifecycleScope.launch { runCatching { goalRepository.refreshGoals(uid, "Habit") } }
    }

    private fun loadGreeting() {
        lifecycleScope.launch {
            getUser()?.name?.trim()?.let { name ->
                val first = name.split(" ").firstOrNull()
                if (!first.isNullOrBlank()) {
                    greeting = "Hi, $first"
                    initial = first.first().uppercase()
                }
            }
        }
    }

    private fun applyGoals(goals: List<Goal>) {
        val today = LocalDate.now()
        val todayIdx = today.dayOfWeek.value % 7
        val scheduled = goals.filter { it.selectedDays.contains(todayIdx) }
        total = scheduled.size
        completed = scheduled.count { (it.progress[today.toString()] ?: 3) == 0 }
        streakDays = goals.maxOfOrNull { it.currentStreak } ?: 0
        habits = scheduled.take(8).map {
            HabitSummary(
                name = it.title,
                category = it.category.ifBlank { it.title },
                streak = it.currentStreak,
                done = (it.progress[today.toString()] ?: 3) == 0,
            )
        }
        weekly = (6 downTo 0).map { offset ->
            val d = today.minusDays(offset.toLong())
            val idx = d.dayOfWeek.value % 7
            val sched = goals.filter { it.selectedDays.contains(idx) }
            if (sched.isEmpty()) 0f
            else sched.count { (it.progress[d.toString()] ?: 3) == 0 }.toFloat() / sched.size
        }
    }
}
