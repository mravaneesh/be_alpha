package com.example.home_ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.PlaylistAddCheck
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.designsystem.components.CoachmarkHost
import com.example.designsystem.components.CoachmarkStep
import com.example.designsystem.components.EmptyState
import com.example.designsystem.components.ErrorState
import com.example.designsystem.components.IconTile
import com.example.designsystem.components.LoadingState
import com.example.designsystem.components.ProgressRing
import com.example.designsystem.components.StatCard
import com.example.designsystem.components.StreakChip
import com.example.designsystem.components.WeeklyBars
import com.example.designsystem.components.coachmarkTarget
import com.example.designsystem.theme.PactGradients
import com.example.designsystem.theme.PactShapeTokens
import com.example.designsystem.theme.PactTheme
import com.example.designsystem.theme.PactType
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing
import java.time.LocalDate
import java.time.format.TextStyle as JTextStyle
import java.util.Locale

/** Plain habit summary the fragment passes in (no Compose types). */
data class HabitSummary(
    val name: String,
    val category: String,
    val streak: Int,
    val done: Boolean,
)

private const val DEFAULT_QUOTE = "Discipline is choosing between what you want now and what you want most."

/**
 * Home / Dashboard. Driven entirely by data the fragment loads — shows a loading spinner while
 * fetching, an error state on failure, and an empty prompt when the user has no habits yet. No
 * placeholder/sample values are shown at runtime (sample data lives only in the previews).
 */
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    greeting: String = "",
    userInitial: String = "",
    completed: Int = 0,
    total: Int = 0,
    streakDays: Int = 0,
    habits: List<HabitSummary> = emptyList(),
    weekly: List<Float> = List(7) { 0f },
    weeklyPercent: Int = 0,
    quote: String = DEFAULT_QUOTE,
    hasAnyGoals: Boolean = true,
    isLoading: Boolean = false,
    error: String? = null,
    runTour: Boolean = false,
    onTourFinished: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onRetry: () -> Unit = {},
) {
  CoachmarkHost(steps = homeTourSteps, enabled = runTour, onFinish = onTourFinished) {
    when {
        isLoading -> LoadingState(modifier.fillMaxSize())
        error != null -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ErrorState(
                icon = Icons.Outlined.CloudOff,
                title = "Couldn't load your day",
                message = error,
                onAction = onRetry,
            )
        }
        else -> {
            val percent = if (total == 0) 0 else (completed * 100) / total
            val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            // Clear the floating bottom nav (pill ~96dp) plus the gesture/nav-bar inset so the last
            // card (e.g. "This week") scrolls fully above the pill instead of hiding under it.
            val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = MaterialTheme.spacing.screen)
                    .padding(top = topInset + MaterialTheme.spacing.md, bottom = bottomInset + 96.dp),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xl),
            ) {
                Header(greeting = greeting.ifBlank { "Welcome" }, initial = userInitial.ifBlank { "?" })
                if (!hasAnyGoals) {
                    Spacer(Modifier.height(MaterialTheme.spacing.xxl))
                    EmptyState(
                        icon = Icons.Outlined.PlaylistAddCheck,
                        title = "No habits yet",
                        message = "Add your first habit in the Habits tab to start building your streak.",
                    )
                } else {
                    DailyFocusCard(
                        percent = percent, completed = completed, total = total, streakDays = streakDays,
                        modifier = Modifier.coachmarkTarget("daily"),
                    )
                    Row(
                        modifier = Modifier.coachmarkTarget("quick"),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
                    ) {
                        StatCard(
                            icon = Icons.Outlined.FitnessCenter,
                            value = "$completed/$total",
                            label = "Habits today",
                            tint = MaterialTheme.semantic.accent,
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            icon = Icons.Outlined.SelfImprovement,
                            value = "$streakDays",
                            label = "Day streak",
                            tint = MaterialTheme.semantic.streak,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (habits.isNotEmpty()) {
                        SectionHeader(title = "Today's habits", action = null)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
                            items(habits) { habit -> HabitMiniCard(habit) }
                        }
                    }
                    WeeklySummaryCard(weekly = weekly, percent = weeklyPercent, modifier = Modifier.coachmarkTarget("weekly"))
                    QuoteCard(quote = quote)
                }
            }
        }
    }
  }
}

private val homeTourSteps = listOf(
    CoachmarkStep("daily", "Your daily progress", "Complete habits to build consistency, one day at a time."),
    CoachmarkStep("quick", "Today at a glance", "Your habits completed today and your current streak."),
    CoachmarkStep("weekly", "Weekly summary", "Track your progress over time and stay motivated."),
)

@Composable
private fun Header(greeting: String, initial: String) {
    val today = LocalDate.now()
    val dateLabel = "${today.dayOfWeek.getDisplayName(JTextStyle.FULL, Locale.getDefault())}, " +
        "${today.month.getDisplayName(JTextStyle.SHORT, Locale.getDefault())} ${today.dayOfMonth}"
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                dateLabel.uppercase(Locale.getDefault()),
                style = PactType.eyebrow,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(MaterialTheme.spacing.xs))
            Text(greeting, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
        }
        AvatarTile(initial)
    }
}

@Composable
private fun RoundIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(PactShapeTokens.iconTile)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun AvatarTile(initial: String) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(PactShapeTokens.iconTile)
            .background(Brush.linearGradient(listOf(Color(0xFF3A3F4A), Color(0xFF1D2026)))),
        contentAlignment = Alignment.Center,
    ) {
        Text(initial, style = MaterialTheme.typography.titleMedium, color = Color.White)
    }
}

@Composable
private fun DailyFocusCard(percent: Int, completed: Int, total: Int, streakDays: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(PactShapeTokens.hero)
            .background(PactGradients.heroSurface())
            .padding(MaterialTheme.spacing.xxxl),
    ) {
        Column {
            Text("DAILY FOCUS", style = PactType.eyebrow, color = MaterialTheme.semantic.accent)
            Spacer(Modifier.height(MaterialTheme.spacing.xl))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(
                    progress = percent / 100f,
                    modifier = Modifier.size(128.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$percent%", style = PactType.statLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text("complete", style = PactType.eyebrow, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.width(MaterialTheme.spacing.xxl))
                Column {
                    Text("Keep the momentum going", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(MaterialTheme.spacing.sm))
                    Text(
                        "You've closed $completed of $total habits today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(MaterialTheme.spacing.md))
                    StreakChip(days = streakDays)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
        if (action != null) {
            Text(action, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.semantic.accent)
        }
    }
}

@Composable
private fun HabitMiniCard(habit: HabitSummary) {
    val (icon, color) = categoryStyle(habit.category)
    Column(
        modifier = Modifier
            .width(128.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .padding(MaterialTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        ProgressRing(progress = if (habit.done) 1f else 0f, modifier = Modifier.size(56.dp), strokeWidth = 6.dp) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        }
        Text(habit.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.semantic.streak, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(MaterialTheme.spacing.xs))
            Text("${habit.streak}d", style = PactType.mono, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun categoryStyle(category: String): Pair<ImageVector, Color> {
    val c = category.lowercase()
    return when {
        "fit" in c || "work" in c || "gym" in c -> Icons.Outlined.FitnessCenter to Color(0xFF4D9BFF)
        "mind" in c || "read" in c || "learn" in c -> Icons.AutoMirrored.Outlined.MenuBook to Color(0xFF8B7BFF)
        "medit" in c -> Icons.Outlined.SelfImprovement to Color(0xFF3DDC97)
        "health" in c || "water" in c || "drink" in c -> Icons.Outlined.LocalDrink to Color(0xFF36C6E0)
        else -> Icons.Outlined.FitnessCenter to Color(0xFF4D9BFF)
    }
}

@Composable
private fun WeeklySummaryCard(weekly: List<Float>, percent: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .padding(MaterialTheme.spacing.xxxl),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("This week", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Text("$percent%", style = PactType.statMedium, color = MaterialTheme.semantic.accent)
        }
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        WeeklyBars(values = weekly, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun QuoteCard(quote: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(PactGradients.quoteSurface())
            .padding(MaterialTheme.spacing.xxxl),
    ) {
        Column {
            Icon(Icons.Filled.FormatQuote, contentDescription = null, tint = MaterialTheme.semantic.accent, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(MaterialTheme.spacing.md))
            Text(quote, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Start)
        }
    }
}

private val sampleHabits = listOf(
    HabitSummary("Workout", "Fitness", 24, true),
    HabitSummary("Read", "Mind", 12, true),
    HabitSummary("Meditate", "Meditate", 8, true),
    HabitSummary("Water", "Health", 48, false),
)

private val defaultWeekly = listOf(0.5f, 0.7f, 0.4f, 0.9f, 0.6f, 0.8f, 0.78f)

@Preview(name = "Dashboard - Light", showBackground = true)
@Composable
private fun DashboardLightPreview() {
    PactTheme(darkTheme = false, dynamicColor = false) {
        DashboardScreen(greeting = "Hi, Alex", userInitial = "A", completed = 3, total = 6, streakDays = 24, habits = sampleHabits, weekly = defaultWeekly, weeklyPercent = 78)
    }
}

@Preview(name = "Dashboard - Dark", showBackground = true)
@Composable
private fun DashboardDarkPreview() {
    PactTheme(darkTheme = true, dynamicColor = false) {
        DashboardScreen(greeting = "Hi, Alex", userInitial = "A", completed = 3, total = 6, streakDays = 24, habits = sampleHabits, weekly = defaultWeekly, weeklyPercent = 78)
    }
}
