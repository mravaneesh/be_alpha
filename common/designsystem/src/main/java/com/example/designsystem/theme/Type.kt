package com.example.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.designsystem.R

/**
 * Apex type system. Inter carries all UI/body/headings; JetBrains Mono carries numerals, labels
 * and eyebrows for a tabular, "instrument-panel" feel. Both are loaded as variable fonts — the
 * weight axis is driven by the [FontWeight] passed to each [Font] (default variationSettings).
 */
val Inter = FontFamily(
    Font(R.font.inter_variable, FontWeight.Normal),
    Font(R.font.inter_variable, FontWeight.Medium),
    Font(R.font.inter_variable, FontWeight.SemiBold),
    Font(R.font.inter_variable, FontWeight.Bold),
    Font(R.font.inter_variable, FontWeight.ExtraBold),
)

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_variable, FontWeight.Medium),
    Font(R.font.jetbrains_mono_variable, FontWeight.SemiBold),
    Font(R.font.jetbrains_mono_variable, FontWeight.Bold),
)

/**
 * Type scale mapped from the redesign spec. Tracking is expressed in em (resolves against size).
 * Headlines are tight (-0.02/-0.03em, weight 700-800); body is calm Inter; mono styles live in
 * [PactType] for numerals/eyebrows that screens opt into explicitly.
 */
val PactTypography: Typography = Typography(
    displayLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.03).em),
    displayMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.03).em),
    displaySmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.ExtraBold, fontSize = 25.sp, lineHeight = 30.sp, letterSpacing = (-0.03).em),
    headlineLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.ExtraBold, fontSize = 27.sp, lineHeight = 32.sp, letterSpacing = (-0.03).em),
    headlineMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.02).em),
    headlineSmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = (-0.02).em),
    titleLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 22.sp, letterSpacing = (-0.02).em),
    titleMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 15.5.sp, lineHeight = 21.sp, letterSpacing = (-0.01).em),
    titleSmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 19.sp, letterSpacing = (-0.01).em),
    bodyLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = (-0.01).em),
    bodyMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = (-0.01).em),
    bodySmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = (-0.01).em),
    labelLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp),
)

/**
 * Mono accents that don't fit M3 slots. Screens apply these directly and tint with the right
 * color. `eyebrow` is meant to be used with UPPERCASE copy.
 */
object PactType {
    val eyebrow = TextStyle(fontFamily = JetBrainsMono, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.16.em)
    val statLarge = TextStyle(fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 34.sp, letterSpacing = (-0.02).em)
    val statMedium = TextStyle(fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 26.sp, letterSpacing = (-0.02).em)
    val mono = TextStyle(fontFamily = JetBrainsMono, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp)
}
