package com.example.goal_ui.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.designsystem.components.CoachmarkHost
import com.example.designsystem.components.CoachmarkStep
import com.example.designsystem.components.EmptyState
import com.example.designsystem.components.Heatmap
import com.example.designsystem.components.LoadingState
import com.example.designsystem.components.TrendChart
import com.example.designsystem.components.WeeklyBars
import com.example.designsystem.components.coachmarkTarget
import com.example.designsystem.theme.PactGradients
import com.example.designsystem.theme.PactType
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing
import com.example.goal_domain.model.Goal
import java.time.LocalDate

private val weekDayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

/**
 * Progress / analytics. Computes its series from the goals' stored `progress`, `bestStreak` and
 * `successRate` — real data, no placeholders. Deeper per-metric wiring is intentionally deferred.
 */
@Composable
fun ProgressScreen(
    goals: List<Goal>,
    isLoading: Boolean,
    onRecap: () -> Unit = {},
    runTour: Boolean = false,
    onTourFinished: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
  CoachmarkHost(steps = statsTourSteps, enabled = runTour, onFinish = onTourFinished) {
    when {
        isLoading && goals.isEmpty() -> LoadingState(modifier)
        goals.isEmpty() -> EmptyState(
            icon = Icons.Outlined.TrendingUp,
            title = "No data yet",
            message = "Track a few habits and your progress will appear here.",
            modifier = modifier,
        )
        else -> {
            val series = remember(goals) { computeSeries(goals) }
            val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = MaterialTheme.spacing.screen,
                        end = MaterialTheme.spacing.screen,
                        top = topInset + MaterialTheme.spacing.screen,
                        bottom = bottomInset + 96.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            ) {
                RecapCard(onClick = onRecap)
                Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
                    HeroStat(
                        modifier = Modifier.weight(1f).coachmarkTarget("streak"),
                        label = "BEST STREAK",
                        value = "${series.bestStreak}",
                        unit = "days",
                        sub = series.bestStreakTitle,
                        icon = Icons.Filled.LocalFireDepartment,
                        tint = MaterialTheme.semantic.streak,
                    )
                    HeroStat(
                        modifier = Modifier.weight(1f),
                        label = "CONSISTENCY",
                        value = "${series.consistency}%",
                        unit = "",
                        sub = "across ${goals.size} habits",
                        icon = Icons.Outlined.TrendingUp,
                        tint = MaterialTheme.semantic.success,
                    )
                }

                AnalyticsCard(title = "Monthly trend", modifier = Modifier.coachmarkTarget("trend")) {
                    TrendChart(points = series.trend, modifier = Modifier.fillMaxWidth())
                }

                AnalyticsCard(title = "This week") {
                    WeeklyBars(values = series.weekly, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(MaterialTheme.spacing.sm))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        weekDayLabels.forEach { Text(it, style = PactType.mono, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }

                AnalyticsCard(title = "Activity", modifier = Modifier.coachmarkTarget("activity")) {
                    Heatmap(intensities = series.heatmap, modifier = Modifier.fillMaxWidth())
                }

                InsightCard(series)
            }
        }
    }
  }
}

private val statsTourSteps = listOf(
    CoachmarkStep("trend", "Your trend", "Track your consistency over days and weeks."),
    CoachmarkStep("streak", "Best streak", "Keep completing habits daily to beat your record."),
    CoachmarkStep("activity", "Activity map", "Every square is a day you showed up."),
)

@Composable
private fun RecapCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(PactGradients.heroSurface())
            .border(BorderStroke(1.dp, MaterialTheme.semantic.hairline), MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .padding(MaterialTheme.spacing.xxxl),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = MaterialTheme.semantic.accent, modifier = Modifier.size(22.dp))
        Spacer(Modifier.size(MaterialTheme.spacing.md))
        Column(Modifier.weight(1f)) {
            Text("Your week in review", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text("See how this week went", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HeroStat(
    modifier: Modifier,
    label: String,
    value: String,
    unit: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(PactGradients.heroSurface())
            .border(BorderStroke(1.dp, MaterialTheme.semantic.hairline), MaterialTheme.shapes.large)
            .padding(MaterialTheme.spacing.xxxl),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Text(label, style = PactType.eyebrow, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = PactType.statLarge, color = MaterialTheme.colorScheme.onSurface)
            if (unit.isNotEmpty()) {
                Spacer(Modifier.size(4.dp))
                Text(unit, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
        Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun AnalyticsCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.dp, MaterialTheme.semantic.hairline), MaterialTheme.shapes.large)
            .padding(MaterialTheme.spacing.xxxl),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        content()
    }
}

@Composable
private fun InsightCard(series: Series) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(PactGradients.quoteSurface())
            .padding(MaterialTheme.spacing.xxxl),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = MaterialTheme.semantic.accent, modifier = Modifier.size(24.dp))
        Spacer(Modifier.size(MaterialTheme.spacing.md))
        Text(series.insight, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

private data class Series(
    val bestStreak: Int,
    val bestStreakTitle: String,
    val consistency: Int,
    val weekly: List<Float>,
    val trend: List<Float>,
    val heatmap: List<Float>,
    val insight: String,
)

/** Daily completion fraction across scheduled goals for a given date (0..1). */
private fun dayFraction(goals: List<Goal>, date: LocalDate): Float {
    val idx = date.dayOfWeek.value % 7
    val scheduled = goals.filter { it.selectedDays.contains(idx) }
    if (scheduled.isEmpty()) return 0f
    val done = scheduled.count { (it.progress[date.toString()] ?: 3) == 0 }
    return done.toFloat() / scheduled.size
}

private fun computeSeries(goals: List<Goal>): Series {
    val today = LocalDate.now()
    val best = goals.maxByOrNull { it.bestStreak }
    val consistency = if (goals.isEmpty()) 0 else goals.map { it.successRate }.average().toInt()

    // Last 7 days, oldest -> today.
    val weekly = (6 downTo 0).map { dayFraction(goals, today.minusDays(it.toLong())) }

    // Last 6 weeks (weekly average), oldest -> newest.
    val trend = (5 downTo 0).map { w ->
        val days = (0..6).map { d -> dayFraction(goals, today.minusDays((w * 7 + d).toLong())) }
        days.average().toFloat()
    }

    // 18 columns (weeks) x 7 rows (days), column-major to match Heatmap indexing.
    val columns = 18
    val rows = 7
    val heatmap = ArrayList<Float>(columns * rows)
    for (c in 0 until columns) {
        val weekStart = (columns - 1 - c) * 7
        for (r in 0 until rows) {
            heatmap.add(dayFraction(goals, today.minusDays((weekStart + (rows - 1 - r)).toLong())))
        }
    }

    val insight = when {
        consistency >= 80 -> "You're on fire — keep the chain alive."
        weekly.takeLast(3).average() > weekly.take(3).average() -> "Trending up this week. Nice momentum."
        else -> "Small wins compound. Close one more today."
    }

    return Series(
        bestStreak = best?.bestStreak ?: 0,
        bestStreakTitle = best?.title?.ifBlank { "—" } ?: "—",
        consistency = consistency,
        weekly = weekly,
        trend = trend,
        heatmap = heatmap,
        insight = insight,
    )
}
