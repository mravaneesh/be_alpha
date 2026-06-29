package com.example.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Product semantics that exceed M3's [androidx.compose.material3.ColorScheme] slots:
 * streak/focus/urgent/cyan accents, the gradient stops, and the hairline border colors used
 * everywhere in the Apex design. Read via `MaterialTheme.semantic`.
 */
data class PactSemantic(
    val accent: Color,
    val accentBright: Color,
    val accentDeep: Color,
    val success: Color,
    val streak: Color,
    val streakWarm: Color,
    val urgent: Color,
    val focus: Color,
    val cyan: Color,
    val hairline: Color,
    val hairlineStrong: Color,
)

internal val PactSemanticDark = PactSemantic(
    accent = ApexPalette.Accent,
    accentBright = ApexPalette.AccentBright,
    accentDeep = ApexPalette.AccentDeep,
    success = ApexPalette.Success,
    streak = ApexPalette.Streak,
    streakWarm = ApexPalette.StreakWarm,
    urgent = ApexPalette.Urgent,
    focus = ApexPalette.Focus,
    cyan = ApexPalette.Cyan,
    hairline = ApexPalette.HairlineDark,
    hairlineStrong = ApexPalette.HairlineStrongDark,
)

internal val PactSemanticLight = PactSemantic(
    accent = ApexPalette.Accent,
    accentBright = ApexPalette.AccentBright,
    accentDeep = ApexPalette.AccentDeep,
    success = ApexPalette.Success,
    streak = ApexPalette.StreakOnLight,
    streakWarm = ApexPalette.StreakWarm,
    urgent = ApexPalette.Urgent,
    focus = ApexPalette.Focus,
    cyan = ApexPalette.Cyan,
    hairline = ApexPalette.HairlineLight,
    hairlineStrong = ApexPalette.HairlineStrongLight,
)

val LocalPactSemantic = staticCompositionLocalOf { PactSemanticDark }

/** Access product semantics: `MaterialTheme.semantic.streak`, etc. */
val MaterialTheme.semantic: PactSemantic
    @Composable @ReadOnlyComposable get() = LocalPactSemantic.current
