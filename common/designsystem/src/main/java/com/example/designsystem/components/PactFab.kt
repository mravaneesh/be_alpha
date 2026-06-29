package com.example.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.PactGradients
import com.example.designsystem.theme.PactShapeTokens
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing

private val OnAccent = Color(0xFF06121F)

/**
 * Single-action gradient FAB. [visible] animates a scale in/out so it only appears where the
 * action applies. 60dp, radius 21, accent gradient, accent-glow shadow.
 */
@Composable
fun PactFab(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
) {
    AnimatedVisibility(visible = visible, enter = scaleIn(), exit = scaleOut()) {
        GradientFab(icon = icon, contentDescription = contentDescription, onClick = onClick, modifier = modifier)
    }
}

/** A quick action revealed by [PactFabMenu]. */
data class PactQuickAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

/**
 * Expanding FAB (Apex spec). The `+` rotates 135deg into an X; the [actions] rise and fade in with
 * a 60ms stagger over a dimmed scrim. Renders as a full-size overlay — place it in a Box that
 * fills the area above the bottom nav. Tapping the scrim or an action closes the menu.
 */
@Composable
fun PactFabMenu(
    actions: List<PactQuickAction>,
    modifier: Modifier = Modifier,
    contentDescription: String = "Quick actions",
) {
    var open by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (open) 135f else 0f,
        animationSpec = tween(durationMillis = 250, easing = PactEasing),
        label = "fabRotate",
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Scrim
        AnimatedVisibility(
            visible = open,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { open = false },
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(end = MaterialTheme.spacing.screen, bottom = 96.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            actions.forEachIndexed { i, action ->
                val delay = (actions.size - 1 - i) * 60
                AnimatedVisibility(
                    visible = open,
                    enter = fadeIn(tween(220, delayMillis = delay)) +
                        slideInVertically(tween(220, delayMillis = delay, easing = PactEasing)) { it / 2 },
                    exit = fadeOut(tween(120)) + slideOutVertically(tween(120)) { it / 2 },
                ) {
                    QuickActionRow(action = action) { open = false }
                }
            }
            Spacer(Modifier.size(MaterialTheme.spacing.xs))
            GradientFab(
                icon = Icons.Filled.Add,
                contentDescription = contentDescription,
                onClick = { open = !open },
                rotation = rotation,
            )
        }
    }
}

@Composable
private fun QuickActionRow(action: PactQuickAction, onConsumed: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 6.dp,
        ) {
            Text(
                action.label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
            )
        }
        Spacer(Modifier.width(MaterialTheme.spacing.md))
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 6.dp,
            modifier = Modifier.size(46.dp),
        ) {
            Box(
                modifier = Modifier.clickable { action.onClick(); onConsumed() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(action.icon, contentDescription = action.label, tint = MaterialTheme.semantic.accent, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun GradientFab(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
) {
    Box(
        modifier = modifier
            .size(60.dp)
            .clip(PactShapeTokens.fab)
            .background(PactGradients.accent())
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = OnAccent,
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer { rotationZ = rotation },
        )
    }
}
