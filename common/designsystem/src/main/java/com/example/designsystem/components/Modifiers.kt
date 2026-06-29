package com.example.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

/** House easing from the spec: cubic-bezier(.2, .8, .2, 1). */
val PactEasing = androidx.compose.animation.core.CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

/**
 * Card / tile press feedback: scales to [pressedScale] on touch-down and springs back on release
 * (spec: scale 0.97, ~180ms ease-out). Pair with the same [interactionSource] you pass to a
 * clickable so the scale tracks real presses.
 */
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.97f,
): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = 180, easing = PactEasing),
        label = "pressScale",
    )
    graphicsLayer { scaleX = scale; scaleY = scale }
}

/** Convenience for a press source you don't otherwise need to read. */
@Composable
fun rememberPressSource(): MutableInteractionSource = remember { MutableInteractionSource() }
