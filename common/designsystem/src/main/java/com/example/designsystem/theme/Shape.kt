package com.example.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Apex radii: chips/buttons 11-14, cards 18-22, hero/sheet 28. Larger, softer corners than the
 * old scale for the premium 2026 feel.
 */
val PactShapes: Shapes = Shapes(
    extraSmall = RoundedCornerShape(11.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** Named one-off shapes that aren't part of the M3 scale (nav pill, FAB, icon tiles). */
object PactShapeTokens {
    val pill = RoundedCornerShape(30.dp)
    val fab = RoundedCornerShape(21.dp)
    val iconTile = RoundedCornerShape(14.dp)
    val hero = RoundedCornerShape(28.dp)
}
