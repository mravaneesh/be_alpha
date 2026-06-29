package com.example.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.PactGradients

/**
 * Primary action — the accent gradient pill from the spec. Dark text rides the bright blue for
 * an accessible (>= 3:1) contrast. 52dp tall (>= 48dp touch target) with press-scale feedback.
 */
@Composable
fun PactPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val source = rememberPressSource()
    val shape = MaterialTheme.shapes.small
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .alpha(if (enabled) 1f else 0.45f)
            .clip(shape)
            .pressScale(source)
            .background(brush = PactGradients.accent())
            .clickable(
                enabled = enabled,
                interactionSource = source,
                indication = ripple(color = Color(0xFF06121F)),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF06121F),
        )
    }
}

/** Low-emphasis text action. */
@Composable
fun PactTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(onClick = onClick, enabled = enabled, modifier = modifier) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
