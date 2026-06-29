package com.example.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.spacing

/** Centered spinner for loading states (req. #10). */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

/**
 * Empty state: an icon, a short title, a guiding line, and an optional action. Copy should
 * invite the next step rather than just stating emptiness (req. #10).
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    StateScaffold(
        icon = icon,
        iconTint = MaterialTheme.colorScheme.primary,
        title = title,
        message = message,
        actionLabel = actionLabel,
        onAction = onAction,
        modifier = modifier,
    )
}

/**
 * Error state: explains what went wrong and offers a retry. Voice stays in-product and
 * specific, never apologetic (req. #10).
 */
@Composable
fun ErrorState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = "Try again",
    onAction: (() -> Unit)? = null,
) {
    StateScaffold(
        icon = icon,
        iconTint = MaterialTheme.colorScheme.error,
        title = title,
        message = message,
        actionLabel = actionLabel,
        onAction = onAction,
        modifier = modifier,
    )
}

@Composable
private fun StateScaffold(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    message: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm, Alignment.CenterVertically),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            PactTextButton(text = actionLabel, onClick = onAction)
        }
    }
}
