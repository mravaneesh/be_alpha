package com.example.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.PactShapeTokens
import com.example.designsystem.theme.semantic

/** One destination in the bottom bar. [id] is the nav graph/destination id it selects. */
data class PactNavItem(
    val id: Int,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
    val badgeCount: Int = 0,
)

/**
 * Floating glass bottom navigation (Apex spec): a frosted, hairline-bordered pill that hovers
 * above the gesture inset. Inactive items are line-icon only; the active item tints to the accent,
 * fills with an accent-tinted pill, and reveals its text label with a 350ms expand. Holds no
 * navigation logic — the caller wires selection to its NavController.
 */
@Composable
fun PactBottomBar(
    items: List<PactNavItem>,
    selectedId: Int,
    onItemSelected: (PactNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.background.luminance() < 0.5f
    // Translucent "glass" fill — content scrolls visibly beneath it.
    val container = if (dark) scheme.surfaceContainer.copy(alpha = 0.85f)
    else scheme.surface.copy(alpha = 0.85f)

    // Just the pill — the Surface is the root; the nav-bar inset lifts it above the gesture area.
    Surface(
        modifier = modifier.windowInsetsPadding(WindowInsets.navigationBars),
        shape = PactShapeTokens.pill,
        color = container,
        border = BorderStroke(1.dp, MaterialTheme.semantic.hairlineStrong),
        shadowElevation = 18.dp,
    ) {
        Row(
            modifier = Modifier
                .height(60.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                PillNavItem(
                    item = item,
                    selected = item.id == selectedId,
                    onClick = { onItemSelected(item) },
                )
            }
        }
    }
}

@Composable
private fun PillNavItem(
    item: PactNavItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val accent = MaterialTheme.semantic.accent
    val inactive = MaterialTheme.colorScheme.onSurfaceVariant
    val pillBg = if (selected) accent.copy(alpha = 0.16f) else androidx.compose.ui.graphics.Color.Transparent

    Row(
        modifier = Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(color = pillBg)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = if (selected) 15.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.icon,
                contentDescription = item.label,
                tint = if (selected) accent else inactive,
                modifier = Modifier.size(22.dp),
            )
            if (item.badgeCount > 0) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 7.dp, y = (-6).dp)
                        .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.semantic.urgent)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (item.badgeCount > 9) "9+" else item.badgeCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = androidx.compose.ui.graphics.Color.White,
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally(animationSpec = tween(350, easing = PactEasing)) +
                fadeIn(animationSpec = tween(350, easing = PactEasing)),
            exit = shrinkHorizontally(animationSpec = tween(350, easing = PactEasing)) +
                fadeOut(animationSpec = tween(350, easing = PactEasing)),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(7.dp))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                )
            }
        }
    }
}
