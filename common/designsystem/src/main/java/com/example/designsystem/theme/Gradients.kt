package com.example.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Brand gradients from the Apex spec. The accent gradient (#6FB0FF -> #4D9BFF at 135deg) is used
 * on the FAB, primary buttons, level badge, progress strokes; the hero gradient is the dark
 * card fill with a radial accent glow bleeding from the top-right.
 */
object PactGradients {

    /** 135deg accent gradient: bright top-left -> base bottom-right. */
    @Composable
    @ReadOnlyComposable
    fun accent(): Brush = Brush.linearGradient(
        colors = listOf(MaterialTheme.semantic.accentBright, MaterialTheme.semantic.accent),
        start = Offset.Zero,
        end = Offset.Infinite,
    )

    /** Dark hero-card fill (160deg) — subtly lifted off the AMOLED background. */
    @Composable
    @ReadOnlyComposable
    fun heroSurface(): Brush {
        val top = if (isDarkScheme()) Color(0xFF11141A) else Color(0xFFFFFFFF)
        val bottom = if (isDarkScheme()) Color(0xFF0B0C10) else Color(0xFFF7F9FC)
        return Brush.linearGradient(
            colors = listOf(top, bottom),
            start = Offset(0f, 0f),
            end = Offset(0f, Float.POSITIVE_INFINITY),
        )
    }

    /** Quote-card fill from the spec (#1A2330 -> #0C1119 dark). */
    @Composable
    @ReadOnlyComposable
    fun quoteSurface(): Brush {
        val top = if (isDarkScheme()) Color(0xFF1A2330) else Color(0xFFEAF1FB)
        val bottom = if (isDarkScheme()) Color(0xFF0C1119) else Color(0xFFDDE8F7)
        return Brush.linearGradient(listOf(top, bottom))
    }

    /** Radial accent glow used behind hero rings / profile banner. Caller supplies center+radius. */
    fun accentGlow(center: Offset, radius: Float, alpha: Float = 0.45f): Brush =
        Brush.radialGradient(
            colors = listOf(ApexPalette.Accent.copy(alpha = alpha), Color.Transparent),
            center = center,
            radius = radius,
        )
}

/** True when the active scheme is the dark one (background is the AMOLED base). */
@Composable
@ReadOnlyComposable
private fun isDarkScheme(): Boolean =
    MaterialTheme.colorScheme.background.luminanceIsDark()

private fun Color.luminanceIsDark(): Boolean {
    val l = 0.2126f * red + 0.7152f * green + 0.0722f * blue
    return l < 0.5f
}
