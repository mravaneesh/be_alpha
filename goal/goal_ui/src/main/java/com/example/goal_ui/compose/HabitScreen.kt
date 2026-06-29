package com.example.goal_ui.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material.icons.outlined.PlaylistAddCheck
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.designsystem.components.CategoryChip
import com.example.designsystem.components.CoachmarkHost
import com.example.designsystem.components.CoachmarkStep
import com.example.designsystem.components.EmptyState
import com.example.designsystem.components.LoadingState
import com.example.designsystem.components.ProgressRing
import com.example.designsystem.components.XpBar
import com.example.designsystem.components.coachmarkTarget
import com.example.designsystem.theme.PactType
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing
import com.example.goal_domain.model.Goal
import java.time.LocalDate

private val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
private const val ALL = "All"

private fun todayIndex(): Int = LocalDate.now().dayOfWeek.value % 7

/**
 * Habit list (Apex). Category filter chips + today's progress + ring habit cards. Pure UI;
 * HabitFragment owns the Firestore/ViewModel actions and passes them in, preserving the existing
 * edit/delete/analytics/status-change behavior.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitScreen(
    goals: List<Goal>,
    isLoading: Boolean,
    onStatusChange: (Goal) -> Unit,
    onUndo: (Goal) -> Unit,
    onEdit: (Goal) -> Unit,
    onDelete: (Goal) -> Unit,
    onAnalytics: (String) -> Unit,
    focusHabitId: String? = null,
    onFocusConsumed: () -> Unit = {},
    runTour: Boolean = false,
    onTourFinished: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var confirmGoal by remember { mutableStateOf<Goal?>(null) }
    var undoGoal by remember { mutableStateOf<Goal?>(null) }
    var selectedCategory by remember { mutableStateOf(ALL) }
    val listState = rememberLazyListState()
    var rippleId by remember { mutableStateOf<String?>(null) }

  CoachmarkHost(steps = habitsTourSteps, enabled = runTour, onFinish = onTourFinished) {
    when {
        isLoading && goals.isEmpty() -> LoadingState(modifier)
        goals.isEmpty() -> EmptyState(
            icon = Icons.Outlined.PlaylistAddCheck,
            title = "No habits yet",
            message = "Add your first habit to start building a streak.",
            modifier = modifier,
        )
        else -> {
            val categories = remember(goals) {
                listOf(ALL) + goals.map { it.category }.filter { it.isNotBlank() }.distinct()
            }
            val visible = if (selectedCategory == ALL) goals
            else goals.filter { it.category == selectedCategory }
            val today = LocalDate.now().toString()
            val scheduled = goals.filter { it.selectedDays.contains(todayIndex()) }
            val doneToday = scheduled.count { (it.progress[today] ?: 3) == 0 }
            // Clear the floating bottom nav so the last habit can scroll above the pill.
            val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

            // Arriving from a widget tap: scroll to that habit and briefly highlight it.
            LaunchedEffect(focusHabitId, visible) {
                val id = focusHabitId ?: return@LaunchedEffect
                val index = visible.indexOfFirst { it.id == id }
                if (index >= 0) {
                    listState.animateScrollToItem(index + 2) // chips + progress items precede the list
                    rippleId = id
                    kotlinx.coroutines.delay(700)
                    rippleId = null
                }
                onFocusConsumed()
            }

            LazyColumn(
                state = listState,
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = MaterialTheme.spacing.screen,
                    end = MaterialTheme.spacing.screen,
                    top = MaterialTheme.spacing.screen,
                    bottom = bottomInset + 104.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            ) {
                item(key = "chips") {
                    LazyRow(
                        modifier = Modifier.coachmarkTarget("filter"),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    ) {
                        items(categories) { cat ->
                            CategoryChip(
                                label = cat,
                                selected = cat == selectedCategory,
                                onClick = { selectedCategory = cat },
                            )
                        }
                    }
                }
                item(key = "progress") {
                    TodayProgressCard(done = doneToday, total = scheduled.size, modifier = Modifier.coachmarkTarget("progress"))
                }
                items(visible, key = { it.id }) { goal ->
                    val isFirst = goal.id == visible.firstOrNull()?.id
                    HabitCard(
                        goal = goal,
                        triggerRipple = goal.id == rippleId,
                        onAnalytics = { onAnalytics(goal.id) },
                        onEdit = { onEdit(goal) },
                        onDelete = { onDelete(goal) },
                        onComplete = { confirmGoal = goal },
                        onUndo = { undoGoal = goal },
                        modifier = Modifier.animateItem().then(if (isFirst) Modifier.coachmarkTarget("habitcard") else Modifier),
                    )
                }
            }
        }
    }
  }

    confirmGoal?.let { goal ->
        AlertDialog(
            onDismissRequest = { confirmGoal = null },
            title = { Text("Complete habit?") },
            text = { Text("Mark \"${goal.title}\" as completed for today?") },
            confirmButton = {
                TextButton(onClick = {
                    onStatusChange(goal)
                    confirmGoal = null
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { confirmGoal = null }) { Text("Cancel") }
            },
        )
    }

    undoGoal?.let { goal ->
        AlertDialog(
            onDismissRequest = { undoGoal = null },
            title = { Text("Undo completion?") },
            text = { Text("Mark \"${goal.title}\" as not done for today?") },
            confirmButton = {
                TextButton(onClick = {
                    onUndo(goal)
                    undoGoal = null
                }) { Text("Undo") }
            },
            dismissButton = {
                TextButton(onClick = { undoGoal = null }) { Text("Cancel") }
            },
        )
    }
}

private val habitsTourSteps = listOf(
    CoachmarkStep("progress", "Today's progress", "Complete your habits to fill this up each day."),
    CoachmarkStep("habitcard", "Build streaks", "Tap the ring to mark a habit done — tap again to undo."),
    CoachmarkStep("filter", "Filter by category", "Jump between habit categories from here."),
)

@Composable
private fun TodayProgressCard(done: Int, total: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.dp, MaterialTheme.semantic.hairline), MaterialTheme.shapes.large)
            .padding(MaterialTheme.spacing.xxxl),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Today's progress", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Text("$done/$total", style = PactType.statMedium, color = MaterialTheme.semantic.accent)
        }
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        XpBar(progress = if (total == 0) 0f else done.toFloat() / total, height = 9.dp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HabitCard(
    goal: Goal,
    triggerRipple: Boolean,
    onAnalytics: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onComplete: () -> Unit,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val status = goal.progress[LocalDate.now().toString()] ?: 3
    val done = status == 0
    val skipped = status == 2
    val (icon, color) = categoryStyle(goal.category)
    val interactionSource = remember { MutableInteractionSource() }

    // Arriving from a widget tap: play a ripple on this card so it reads as "tapped".
    LaunchedEffect(triggerRipple) {
        if (triggerRipple) {
            val press = PressInteraction.Press(Offset.Zero)
            interactionSource.emit(press)
            kotlinx.coroutines.delay(220)
            interactionSource.emit(PressInteraction.Release(press))
        }
    }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(if (done) color.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface)
                .border(
                    BorderStroke(1.dp, if (done) color.copy(alpha = 0.5f) else MaterialTheme.semantic.hairline),
                    MaterialTheme.shapes.large,
                )
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    onClick = onAnalytics,
                    onLongClick = { menuOpen = true },
                )
                .padding(MaterialTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HabitRing(done = done, icon = icon, color = color, onClick = if (done) onUndo else onComplete)
            Spacer(Modifier.width(MaterialTheme.spacing.lg))
            Column(Modifier.weight(1f)) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (skipped) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (skipped) TextDecoration.LineThrough else null,
                    maxLines = 1,
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (goal.category.isNotBlank()) {
                        Text(goal.category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("  ·  ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.semantic.streak, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("${goal.currentStreak}d", style = PactType.mono, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (goal.freezesAvailable > 0) {
                        Text("  ·  ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Icon(Icons.Outlined.AcUnit, contentDescription = "Streak freezes", tint = Color(0xFF36C6E0), modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("${goal.freezesAvailable}", style = PactType.mono, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View analytics",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(
                text = { Text("Edit") },
                leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                onClick = { menuOpen = false; onEdit() },
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                onClick = { menuOpen = false; onDelete() },
            )
        }
    }
}

/** 50dp tappable ring — accent-filled check when done (tap to undo), category icon over an outline otherwise. */
@Composable
private fun HabitRing(done: Boolean, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        ProgressRing(progress = if (done) 1f else 0f, modifier = Modifier.size(50.dp), strokeWidth = 4.dp) {
            Icon(
                imageVector = if (done) Icons.Filled.Check else icon,
                contentDescription = if (done) "Completed" else "Mark complete",
                tint = if (done) MaterialTheme.semantic.accent else color,
                modifier = Modifier.size(22.dp),
            )
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
        "disciplin" in c || "sugar" in c || "no " in c -> Icons.Outlined.Block to Color(0xFFFF7A45)
        "sleep" in c || "recover" in c -> Icons.Outlined.Bedtime to Color(0xFFFFB259)
        else -> Icons.Outlined.FavoriteBorder to Color(0xFF4D9BFF)
    }
}
