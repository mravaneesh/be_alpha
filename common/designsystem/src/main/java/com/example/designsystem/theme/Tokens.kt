package com.example.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Apex spacing scale (px -> dp): 4 / 6 / 8 / 11 / 13 / 16 / 18 / 20 / 22 / 26.
 * Screen horizontal padding is [screen] = 22.dp. Use these instead of inline dp values.
 */
data class PactSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 6.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 11.dp,
    val lg: Dp = 13.dp,
    val xl: Dp = 16.dp,
    val xxl: Dp = 18.dp,
    val xxxl: Dp = 22.dp,
    val huge: Dp = 26.dp,
    val screen: Dp = 22.dp,
)

/**
 * Elevation tokens. Apex leans on color/hairline borders over Material shadows for depth on the
 * AMOLED surfaces, so card elevation is intentionally low; [fab]/[hero] carry the accent glow.
 */
data class PactElevation(
    val none: Dp = 0.dp,
    val card: Dp = 0.dp,
    val raised: Dp = 4.dp,
    val overlay: Dp = 12.dp,
    val fab: Dp = 10.dp,
    val hero: Dp = 6.dp,
)

val LocalPactSpacing = staticCompositionLocalOf { PactSpacing() }
val LocalPactElevation = staticCompositionLocalOf { PactElevation() }

/** `MaterialTheme.spacing.md`, mirroring how `MaterialTheme.colorScheme` is consumed. */
val MaterialTheme.spacing: PactSpacing
    @Composable @ReadOnlyComposable get() = LocalPactSpacing.current

val MaterialTheme.elevation: PactElevation
    @Composable @ReadOnlyComposable get() = LocalPactElevation.current
