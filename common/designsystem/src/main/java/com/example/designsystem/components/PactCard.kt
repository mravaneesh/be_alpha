package com.example.designsystem.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.elevation
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing

/**
 * Standard surface card — Apex style: rounded (large radius), flat on the AMOLED surface with a
 * hairline border for definition instead of a heavy shadow, generous inner padding, and automatic
 * size animation for smooth expand/collapse. Pass [onClick] to make it pressable (adds the
 * scale-0.97 press feedback from the spec).
 */
@Composable
fun PactCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    onClick: (() -> Unit)? = null,
    border: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = MaterialTheme.shapes.large
    val colors = CardDefaults.cardColors(
        containerColor = containerColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )
    val stroke = if (border) BorderStroke(1.dp, MaterialTheme.semantic.hairline) else null
    val elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.elevation.card)

    if (onClick != null) {
        val source = rememberPressSource()
        Card(
            onClick = onClick,
            interactionSource = source,
            modifier = modifier.animateContentSize().pressScale(source),
            shape = shape,
            colors = colors,
            border = stroke,
            elevation = elevation,
        ) {
            Column(modifier = Modifier.padding(MaterialTheme.spacing.xl), content = content)
        }
    } else {
        Card(
            modifier = modifier.animateContentSize(),
            shape = shape,
            colors = colors,
            border = stroke,
            elevation = elevation,
        ) {
            Column(modifier = Modifier.padding(MaterialTheme.spacing.xl), content = content)
        }
    }
}
