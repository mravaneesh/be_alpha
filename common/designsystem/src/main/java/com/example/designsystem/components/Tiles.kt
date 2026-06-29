package com.example.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.PactShapeTokens
import com.example.designsystem.theme.PactType
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing

/** Rounded tinted icon tile (the small colored square behind category/stat icons). */
@Composable
fun IconTile(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    shape: androidx.compose.ui.graphics.Shape = PactShapeTokens.iconTile,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color = tint.copy(alpha = 0.16f), shape = shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

/** Streak chip: warm fire icon + "{n} day streak" on a tinted pill. */
@Composable
fun StreakChip(days: Int, modifier: Modifier = Modifier, label: String = "$days day streak") {
    val streak = MaterialTheme.semantic.streak
    Row(
        modifier = modifier
            .background(color = streak.copy(alpha = 0.13f), shape = PactShapeTokens.pill)
            .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = streak, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(MaterialTheme.spacing.xs))
        Text(label, style = PactType.mono.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize), color = streak)
    }
}

/** Compact metric card: tinted icon, big mono number, label. */
@Composable
fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.dp, MaterialTheme.semantic.hairline), MaterialTheme.shapes.large)
            .padding(MaterialTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        IconTile(icon = icon, tint = tint)
        Text(value, style = PactType.statMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Filter chip: active = accent fill + dark text; inactive = surface variant + secondary text. */
@Composable
fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg by animateColorAsState(
        if (selected) MaterialTheme.semantic.accent else MaterialTheme.colorScheme.surfaceVariant,
        label = "chipBg",
    )
    val fg = if (selected) Color(0xFF06121F) else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .clip(PactShapeTokens.pill)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = MaterialTheme.spacing.xl, vertical = MaterialTheme.spacing.md),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = fg)
    }
}

/** XP / level progress bar with the accent gradient fill. [progress] is 0..1. */
@Composable
fun XpBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(PactShapeTokens.pill)
            .background(MaterialTheme.semantic.hairlineStrong),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(height)
                .clip(PactShapeTokens.pill)
                .background(Brush.horizontalGradient(listOf(MaterialTheme.semantic.accentBright, MaterialTheme.semantic.accent))),
        )
    }
}

/** Gradient badge tile for achievements; [unlocked] = false dims it. */
@Composable
fun AchievementTile(
    icon: ImageVector,
    label: String,
    unlocked: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 62.dp,
) {
    Column(
        modifier = modifier.alpha(if (unlocked) 1f else 0.4f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(PactShapeTokens.iconTile)
                .background(
                    if (unlocked) Brush.linearGradient(listOf(MaterialTheme.semantic.accentBright, MaterialTheme.semantic.accent))
                    else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = if (unlocked) Color(0xFF06121F) else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(size * 0.45f))
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
