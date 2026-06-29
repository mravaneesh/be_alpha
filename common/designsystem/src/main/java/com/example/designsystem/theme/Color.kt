package com.example.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * "Apex" palette — the 2026 visual redesign. Blue accent (#4D9BFF) + gradients on an AMOLED
 * true-black surface stack (dark) / soft cool-gray (light). Hand-tuned, not auto-inverted.
 *
 * M3's [ColorScheme] only has so many slots; product-specific semantics (streak, focus, urgent,
 * cyan, the hairline borders, gradient stops) live in [PactSemantic] and are provided via a
 * composition local — see Theme.kt and `MaterialTheme.semantic`.
 */
internal object ApexPalette {
    // Shared accent + gradient stops
    val Accent = Color(0xFF4D9BFF)
    val AccentBright = Color(0xFF6FB0FF)
    val AccentDeep = Color(0xFF2F6FE0)

    // Shared semantic
    val Success = Color(0xFF3DDC97)
    val Streak = Color(0xFFFF9F45)
    val StreakWarm = Color(0xFFFFB259)
    val StreakOnLight = Color(0xFFE07C1E)
    val Urgent = Color(0xFFFF5D5D)
    val Focus = Color(0xFF8B7BFF)
    val Cyan = Color(0xFF36C6E0)

    // Dark — AMOLED stack
    val BgDark = Color(0xFF000000)
    val Surface1Dark = Color(0xFF0D0E11)
    val Surface2Dark = Color(0xFF16181D)
    val Surface3Dark = Color(0xFF1D2026)
    val HairlineDark = Color(0x0FFFFFFF)        // rgba(255,255,255,.06)
    val HairlineStrongDark = Color(0x1AFFFFFF)  // rgba(255,255,255,.10)
    val Text1Dark = Color(0xFFF4F5F7)
    val Text2Dark = Color(0xFF9DA3AF)
    val Text3Dark = Color(0xFF697080)
    val Text4Dark = Color(0xFF5B606B)
    val AccentContainerDark = Color(0xFF16304D)
    val OnAccentContainerDark = Color(0xFFBBD7FF)

    // Light
    val BgLight = Color(0xFFF4F5F7)
    val SurfaceLight = Color(0xFFFFFFFF)
    val SurfaceVariantLight = Color(0xFFEDEFF2)
    val HairlineLight = Color(0x14000000)       // rgba(0,0,0,.08)
    val HairlineStrongLight = Color(0x1F000000)
    val Text1Light = Color(0xFF15171C)
    val Text2Light = Color(0xFF6B7280)
    val Text3Light = Color(0xFF8A909C)
    val AccentContainerLight = Color(0xFFDCE9FF)
    val OnAccentContainerLight = Color(0xFF0A335F)

    val NearBlack = Color(0xFF06121F)
    val White = Color(0xFFFFFFFF)
}

/** Light scheme — soft cool-gray canvas, white cards, blue accent (deepened for contrast). */
val PactLightColorScheme: ColorScheme = lightColorScheme(
    primary = ApexPalette.AccentDeep,
    onPrimary = ApexPalette.White,
    primaryContainer = ApexPalette.AccentContainerLight,
    onPrimaryContainer = ApexPalette.OnAccentContainerLight,
    secondary = ApexPalette.Focus,
    onSecondary = ApexPalette.White,
    secondaryContainer = Color(0xFFE6E2FF),
    onSecondaryContainer = Color(0xFF2A2160),
    tertiary = Color(0xFF16A472),
    onTertiary = ApexPalette.White,
    error = Color(0xFFE5484D),
    onError = ApexPalette.White,
    background = ApexPalette.BgLight,
    onBackground = ApexPalette.Text1Light,
    surface = ApexPalette.SurfaceLight,
    onSurface = ApexPalette.Text1Light,
    surfaceVariant = ApexPalette.SurfaceVariantLight,
    onSurfaceVariant = ApexPalette.Text2Light,
    surfaceContainerLowest = ApexPalette.White,
    surfaceContainerLow = Color(0xFFFAFBFC),
    surfaceContainer = Color(0xFFF1F3F5),
    surfaceContainerHigh = Color(0xFFEAEDF0),
    surfaceContainerHighest = Color(0xFFE3E7EB),
    outline = Color(0xFFD7DBE0),
    outlineVariant = Color(0xFFE7EAEE),
    scrim = Color(0xFF000000),
)

/** Dark scheme — AMOLED true black surfaces, brighter blue accent. */
val PactDarkColorScheme: ColorScheme = darkColorScheme(
    primary = ApexPalette.Accent,
    onPrimary = ApexPalette.NearBlack,
    primaryContainer = ApexPalette.AccentContainerDark,
    onPrimaryContainer = ApexPalette.OnAccentContainerDark,
    secondary = ApexPalette.Focus,
    onSecondary = ApexPalette.NearBlack,
    secondaryContainer = Color(0xFF2A2160),
    onSecondaryContainer = Color(0xFFD9D2FF),
    tertiary = ApexPalette.Success,
    onTertiary = ApexPalette.NearBlack,
    error = ApexPalette.Urgent,
    onError = ApexPalette.NearBlack,
    background = ApexPalette.BgDark,
    onBackground = ApexPalette.Text1Dark,
    surface = ApexPalette.Surface1Dark,
    onSurface = ApexPalette.Text1Dark,
    surfaceVariant = ApexPalette.Surface2Dark,
    onSurfaceVariant = ApexPalette.Text2Dark,
    surfaceContainerLowest = ApexPalette.BgDark,
    surfaceContainerLow = ApexPalette.Surface1Dark,
    surfaceContainer = ApexPalette.Surface2Dark,
    surfaceContainerHigh = ApexPalette.Surface3Dark,
    surfaceContainerHighest = Color(0xFF23262D),
    outline = ApexPalette.HairlineStrongDark,
    outlineVariant = ApexPalette.HairlineDark,
    scrim = Color(0xFF000000),
)
