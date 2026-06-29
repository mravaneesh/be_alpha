package com.example.goal_ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.designsystem.components.EmptyState
import com.example.designsystem.components.LoadingState
import com.example.designsystem.components.ProgressRing
import com.example.designsystem.components.StatCard
import com.example.designsystem.components.WeeklyBars
import com.example.designsystem.theme.PactType
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing
import com.example.goal_domain.model.Goal
import com.example.goal_domain.usecase.HabitCompletion
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private data class WeekRecap(
    val rangeLabel: String,
    val completionPercent: Int,
    val completed: Int,
    val bestStreak: Int,
    val deltaPercent: Int,
    val dayValues: List<Float>,
    val todayIndex: Int,
    val mostConsistent: String?,
    val headline: String,
)

@Composable
fun WeeklyRecapScreen(
    goals: List<Goal>,
    isLoading: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        isLoading && goals.isEmpty() -> LoadingState(modifier)
        goals.isEmpty() -> EmptyState(
            icon = Icons.Outlined.TrendingUp,
            title = "No recap yet",
            message = "Track habits this week and your recap will appear here.",
            modifier = modifier,
        )
        else -> {
            val recap = remember(goals) { computeRecap(goals) }
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = MaterialTheme.spacing.screen)
                    .padding(bottom = MaterialTheme.spacing.xxl),
            ) {
                Row(modifier = Modifier.statusBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Column {
                        Text("Your week in review", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
                        Text(recap.rangeLabel, style = PactType.mono, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(MaterialTheme.spacing.xl))

                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ProgressRing(progress = recap.completionPercent / 100f, modifier = Modifier.size(150.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${recap.completionPercent}%", style = PactType.statLarge, color = MaterialTheme.colorScheme.onSurface)
                            Text("completed", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(Modifier.height(MaterialTheme.spacing.lg))
                Text(recap.headline, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(MaterialTheme.spacing.xl))
                Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
                    StatCard(icon = Icons.Outlined.CheckCircle, value = "${recap.completed}", label = "Completed", tint = MaterialTheme.semantic.success, modifier = Modifier.weight(1f))
                    StatCard(icon = Icons.Filled.LocalFireDepartment, value = "${recap.bestStreak}", label = "Best streak", tint = MaterialTheme.semantic.streak, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(MaterialTheme.spacing.md))
                val deltaText = if (recap.deltaPercent >= 0) "+${recap.deltaPercent}%" else "${recap.deltaPercent}%"
                StatCard(
                    icon = Icons.Outlined.TrendingUp,
                    value = deltaText,
                    label = "vs last week",
                    tint = if (recap.deltaPercent >= 0) MaterialTheme.semantic.success else MaterialTheme.semantic.urgent,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(MaterialTheme.spacing.xl))
                Text("This week", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(MaterialTheme.spacing.md))
                WeeklyBars(values = recap.dayValues, todayIndex = recap.todayIndex)

                if (recap.mostConsistent != null) {
                    Spacer(Modifier.height(MaterialTheme.spacing.xl))
                    Text("Most consistent", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(recap.mostConsistent, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.semantic.accent)
                }
            }
        }
    }
}

private fun computeRecap(goals: List<Goal>): WeekRecap {
    val today = LocalDate.now()
    val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())

    fun doneOn(g: Goal, d: LocalDate) = HabitCompletion.isDoneOn(g, d)
    fun scheduledOn(g: Goal, d: LocalDate) = g.selectedDays.contains(d.dayOfWeek.value % 7)

    // Per-day ratios across the full week (for the bar chart).
    val dayValues = (0..6).map { i ->
        val date = monday.plusDays(i.toLong())
        val sched = goals.count { scheduledOn(it, date) }
        if (sched == 0) 0f else goals.count { scheduledOn(it, date) && doneOn(it, date) }.toFloat() / sched
    }

    // Totals Monday..today (don't count future days).
    var done = 0
    var scheduled = 0
    var d = monday
    while (!d.isAfter(today)) {
        scheduled += goals.count { scheduledOn(it, d) }
        done += goals.count { scheduledOn(it, d) && doneOn(it, d) }
        d = d.plusDays(1)
    }
    val percent = if (scheduled == 0) 0 else done * 100 / scheduled

    // Last week (full Mon..Sun) for comparison.
    val lastMonday = monday.minusWeeks(1)
    var lastDone = 0
    var lastScheduled = 0
    var ld = lastMonday
    val lastSunday = lastMonday.plusDays(6)
    while (!ld.isAfter(lastSunday)) {
        lastScheduled += goals.count { scheduledOn(it, ld) }
        lastDone += goals.count { scheduledOn(it, ld) && doneOn(it, ld) }
        ld = ld.plusDays(1)
    }
    val lastPercent = if (lastScheduled == 0) 0 else lastDone * 100 / lastScheduled

    // Most consistent habit this week.
    val mostConsistent = goals.maxByOrNull { g ->
        var s = 0; var c = 0; var x = monday
        while (!x.isAfter(today)) { if (scheduledOn(g, x)) { s++; if (doneOn(g, x)) c++ }; x = x.plusDays(1) }
        if (s == 0) -1f else c.toFloat() / s
    }?.takeIf { g ->
        var x = monday; var any = false
        while (!x.isAfter(today)) { if (scheduledOn(g, x) && doneOn(g, x)) any = true; x = x.plusDays(1) }
        any
    }?.title

    val headline = when {
        percent >= 80 -> "Outstanding week! 🎉"
        percent >= 50 -> "Solid week — keep it going."
        percent > 0 -> "Every rep counts. Finish strong."
        else -> "A fresh week is ahead."
    }

    val mon = monday.format()
    val sun = monday.plusDays(6).format()

    return WeekRecap(
        rangeLabel = "$mon – $sun",
        completionPercent = percent,
        completed = done,
        bestStreak = goals.maxOfOrNull { it.currentStreak } ?: 0,
        deltaPercent = percent - lastPercent,
        dayValues = dayValues,
        todayIndex = today.dayOfWeek.value - 1,
        mostConsistent = mostConsistent,
        headline = headline,
    )
}

private fun LocalDate.format(): String {
    val month = month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    return "$month $dayOfMonth"
}
