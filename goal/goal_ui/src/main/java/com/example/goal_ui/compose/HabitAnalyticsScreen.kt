package com.example.goal_ui.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.designsystem.components.Heatmap
import com.example.designsystem.components.StatCard
import com.example.designsystem.components.TrendChart
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing
import com.example.goal_domain.model.Goal
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

/**
 * Per-habit analytics in the Apex style — real streak / success / completion stats, a weekly
 * success trend, and an activity heatmap derived from the goal's stored progress. The old
 * social "post mode" (capturing sections into a post) is out of the current design scope.
 */
@Composable
fun HabitAnalyticsScreen(
    goal: Goal,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val trend = remember(goal) { weeklyTrend(goal) }
    val heat = remember(goal) { heatmap(goal) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.screen)
            .padding(bottom = MaterialTheme.spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            Text(goal.title.ifBlank { "Habit" }, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
            StatCard(Icons.Filled.LocalFireDepartment, "${goal.currentStreak}", "Current streak", MaterialTheme.semantic.streak, Modifier.weight(1f))
            StatCard(Icons.Outlined.EmojiEvents, "${goal.bestStreak}", "Best streak", MaterialTheme.semantic.accent, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
            StatCard(Icons.Outlined.TrendingUp, "${goal.successRate}%", "Success rate", MaterialTheme.semantic.success, Modifier.weight(1f))
            StatCard(Icons.Filled.CheckCircle, "${goal.totalCompleted}", "Total done", MaterialTheme.semantic.cyan, Modifier.weight(1f))
        }

        DetailsBlock(goal)
        AnalyticsBlock(title = "Success trend") {
            TrendChart(points = trend, modifier = Modifier.fillMaxWidth())
        }
        AnalyticsBlock(title = "History") {
            MonthCalendar(goal)
        }
        AnalyticsBlock(title = "Activity") {
            Heatmap(intensities = heat, modifier = Modifier.fillMaxWidth())
        }
    }
}

private val dayShort = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

@Composable
private fun DetailsBlock(goal: Goal) {
    val schedule = when {
        goal.selectedDays.size >= 7 -> "Every day"
        goal.selectedDays.isEmpty() -> "No days set"
        else -> goal.selectedDays.sorted().joinToString(", ") { dayShort.getOrElse(it) { "?" } }
    }
    AnalyticsBlock(title = "Details") {
        DetailRow("Schedule", schedule)
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        DetailRow("Reminder", goal.reminder.ifBlank { "Off" })
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        DetailRow("Streak freezes", "${goal.freezesAvailable}")
        if (goal.category.isNotBlank()) {
            Spacer(Modifier.height(MaterialTheme.spacing.md))
            DetailRow("Category", goal.category)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

private enum class DayStatus { DONE, MISSED, FROZEN, TODAY, OFF, FUTURE }

@Composable
private fun MonthCalendar(goal: Goal) {
    var month by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()

    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { month = month.minusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "${month.month.getDisplayName(JavaTextStyle.FULL, Locale.getDefault())} ${month.year}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            val canForward = month < YearMonth.now()
            IconButton(onClick = { if (canForward) month = month.plusMonths(1) }, enabled = canForward) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month", tint = if (canForward) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.semantic.hairlineStrong)
            }
        }
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Row(Modifier.fillMaxWidth()) {
            dayShort.forEach { d ->
                Text(d.take(1), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(MaterialTheme.spacing.xs))

        val firstOfMonth = month.atDay(1)
        val startOffset = firstOfMonth.dayOfWeek.value % 7 // 0 = Sunday
        val daysInMonth = month.lengthOfMonth()
        val cells = (0 until startOffset).map { null } + (1..daysInMonth).map { month.atDay(it) }
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                for (i in 0 until 7) {
                    val date = week.getOrNull(i)
                    Box(Modifier.weight(1f).padding(2.dp), contentAlignment = Alignment.Center) {
                        if (date != null) DayCell(date, statusFor(goal, date, today))
                    }
                }
            }
        }
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
            LegendDot(MaterialTheme.semantic.accent, "Done")
            LegendDot(MaterialTheme.semantic.urgent, "Missed")
            LegendDot(MaterialTheme.semantic.cyan, "Frozen")
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, status: DayStatus) {
    val accent = MaterialTheme.semantic.accent
    val (bg, fg) = when (status) {
        DayStatus.DONE -> accent to androidx.compose.ui.graphics.Color(0xFF06121F)
        DayStatus.MISSED -> MaterialTheme.semantic.urgent.copy(alpha = 0.20f) to MaterialTheme.colorScheme.onSurface
        DayStatus.FROZEN -> MaterialTheme.semantic.cyan.copy(alpha = 0.22f) to MaterialTheme.colorScheme.onSurface
        DayStatus.TODAY -> androidx.compose.ui.graphics.Color.Transparent to accent
        DayStatus.OFF -> androidx.compose.ui.graphics.Color.Transparent to MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        DayStatus.FUTURE -> androidx.compose.ui.graphics.Color.Transparent to MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(bg)
            .then(if (status == DayStatus.TODAY) Modifier.border(BorderStroke(1.5.dp, accent), androidx.compose.foundation.shape.CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text("${date.dayOfMonth}", style = MaterialTheme.typography.labelMedium, color = fg)
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(color))
        Spacer(Modifier.size(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun statusFor(goal: Goal, date: LocalDate, today: LocalDate): DayStatus = when {
    date.isAfter(today) -> DayStatus.FUTURE
    !scheduled(goal, date) -> DayStatus.OFF
    isDone(goal, date) -> DayStatus.DONE
    goal.progress[date.toString()] == 4 -> DayStatus.FROZEN
    date.isEqual(today) -> DayStatus.TODAY
    else -> DayStatus.MISSED
}

@Composable
private fun AnalyticsBlock(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
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

private fun isDone(goal: Goal, date: LocalDate): Boolean =
    (goal.progress[date.toString()] ?: 3) == 0

private fun scheduled(goal: Goal, date: LocalDate): Boolean =
    goal.selectedDays.contains(date.dayOfWeek.value % 7)

/** Weekly success fraction over the last 6 weeks (oldest -> newest). */
private fun weeklyTrend(goal: Goal): List<Float> {
    val today = LocalDate.now()
    return (5 downTo 0).map { w ->
        val days = (0..6).map { d -> today.minusDays((w * 7 + d).toLong()) }
        val sched = days.filter { scheduled(goal, it) }
        if (sched.isEmpty()) 0f else sched.count { isDone(goal, it) }.toFloat() / sched.size
    }
}

/** 18x7 completion grid (column-major) for the heatmap. */
private fun heatmap(goal: Goal): List<Float> {
    val today = LocalDate.now()
    val columns = 18
    val rows = 7
    val out = ArrayList<Float>(columns * rows)
    for (c in 0 until columns) {
        val weekStart = (columns - 1 - c) * 7
        for (r in 0 until rows) {
            val date = today.minusDays((weekStart + (rows - 1 - r)).toLong())
            out.add(if (isDone(goal, date)) 1f else 0f)
        }
    }
    return out
}
