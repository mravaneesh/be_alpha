package com.example.profile_ui.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.designsystem.components.AchievementTile
import com.example.designsystem.components.CoachmarkHost
import com.example.designsystem.components.CoachmarkStep
import com.example.designsystem.components.LoadingState
import com.example.designsystem.components.PactPrimaryButton
import com.example.designsystem.components.XpBar
import com.example.designsystem.components.coachmarkTarget
import com.example.designsystem.theme.PactShapeTokens
import com.example.designsystem.theme.PactType
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing
import com.example.profile_domain.model.UserProfile

/** A single derived achievement (computed from real habit data in the fragment). */
data class Achievement(val name: String, val icon: ImageVector, val unlocked: Boolean)

/** Gamification stats derived from the user's real habits. */
data class ProfileStats(
    val daysActive: Int = 0,
    val bestStreak: Int = 0,
    val totalCompleted: Int = 0,
    val xp: Int = 0,
    val level: Int = 1,
    val levelTitle: String = "Beginner",
    val xpToNext: Int = 500,
    val levelProgress: Float = 0f,
    val achievements: List<Achievement> = emptyList(),
)

@Composable
fun ProfileScreen(
    profile: UserProfile,
    stats: ProfileStats,
    isLoading: Boolean,
    onEdit: () -> Unit,
    onSettings: () -> Unit,
    runTour: Boolean = false,
    onTourFinished: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (isLoading) {
        LoadingState(modifier); return
    }
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
  CoachmarkHost(steps = profileTourSteps, enabled = runTour, onFinish = onTourFinished) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = bottomInset + 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Header(profile = profile, stats = stats, onSettings = onSettings)

        Column(Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.spacing.screen)) {
            Spacer(Modifier.height(MaterialTheme.spacing.xl))
            XpCard(stats)
            Spacer(Modifier.height(MaterialTheme.spacing.xl))
            StatTrio(stats, modifier = Modifier.coachmarkTarget("stats"))
            Spacer(Modifier.height(MaterialTheme.spacing.xxl))
            Achievements(stats.achievements)
            Spacer(Modifier.height(MaterialTheme.spacing.xxl))
            PactPrimaryButton(text = "Edit profile", onClick = onEdit)
        }
    }
  }
}

private val profileTourSteps = listOf(
    CoachmarkStep("stats", "Your stats", "Your lifetime streaks, days active and habits completed."),
    CoachmarkStep("settings", "Settings", "Customize your experience, including notifications and walkthroughs."),
)

@Composable
private fun Header(profile: UserProfile, stats: ProfileStats, onSettings: () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(Brush.verticalGradient(listOf(MaterialTheme.semantic.accent.copy(alpha = 0.22f), Color.Transparent))),
        )
        IconButton(onClick = onSettings, modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(MaterialTheme.spacing.sm).coachmarkTarget("settings")) {
            Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
        }
        Column(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = MaterialTheme.spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (profile.profileImageUrl.isNotBlank()) {
                AsyncImage(
                    model = profile.profileImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(88.dp).clip(PactShapeTokens.hero),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(PactShapeTokens.hero)
                        .background(Brush.linearGradient(listOf(Color(0xFF3A3F4A), Color(0xFF1D2026)))),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(profile.name.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.displaySmall, color = Color.White)
                }
            }
            Spacer(Modifier.height(MaterialTheme.spacing.md))
            Text(profile.name.ifBlank { "You" }, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
            Text("@${profile.userName}", style = PactType.mono, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(MaterialTheme.spacing.md))
            LevelBadge(level = stats.level, title = stats.levelTitle)
            if (profile.bio.isNotBlank()) {
                Spacer(Modifier.height(MaterialTheme.spacing.md))
                Text(
                    profile.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.huge),
                )
            }
        }
    }
}

@Composable
private fun LevelBadge(level: Int, title: String) {
    Row(
        modifier = Modifier
            .clip(PactShapeTokens.pill)
            .background(Brush.linearGradient(listOf(MaterialTheme.semantic.accentBright, MaterialTheme.semantic.accent)))
            .padding(horizontal = MaterialTheme.spacing.xl, vertical = MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = Color(0xFF06121F), modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(MaterialTheme.spacing.xs))
        Text("Level $level · $title", style = MaterialTheme.typography.labelLarge, color = Color(0xFF06121F))
    }
}

@Composable
private fun XpCard(stats: ProfileStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.dp, MaterialTheme.semantic.hairline), MaterialTheme.shapes.large)
            .padding(MaterialTheme.spacing.xxxl),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text("${stats.xp}", style = PactType.statMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(" XP", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 3.dp).weight(1f))
            Text("${stats.xpToNext} to Lv ${stats.level + 1}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        XpBar(progress = stats.levelProgress)
    }
}

@Composable
private fun StatTrio(stats: ProfileStats, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.dp, MaterialTheme.semantic.hairline), MaterialTheme.shapes.large)
            .padding(vertical = MaterialTheme.spacing.xl),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatItem("${stats.daysActive}", "Days active", MaterialTheme.colorScheme.onSurface, Modifier.weight(1f))
        StatDivider()
        StatItem("${stats.bestStreak}", "Best streak", MaterialTheme.semantic.streak, Modifier.weight(1f))
        StatDivider()
        StatItem("${stats.totalCompleted}", "Completed", MaterialTheme.semantic.success, Modifier.weight(1f))
    }
}

@Composable
private fun StatDivider() {
    Box(
        Modifier
            .height(34.dp)
            .width(1.dp)
            .background(MaterialTheme.semantic.hairline),
    )
}

@Composable
private fun StatItem(value: String, label: String, valueColor: Color, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = PactType.statMedium, color = valueColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Achievements(items: List<Achievement>) {
    val unlocked = items.count { it.unlocked }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Achievements", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
        Text("$unlocked of ${items.size}", style = PactType.mono, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Spacer(Modifier.height(MaterialTheme.spacing.md))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
        items(items) { a -> AchievementTile(icon = a.icon, label = a.name, unlocked = a.unlocked) }
    }
}
