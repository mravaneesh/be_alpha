package com.example.authentication.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.PactShapeTokens
import com.example.designsystem.theme.PactType
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing

/**
 * Welcome / Intro screen. Single entry point: Continue with Google. The fragment owns the
 * Google sign-in flow and toggles [isLoading].
 */
@Composable
fun WelcomeScreen(
    onContinueWithGoogle: () -> Unit,
    onTerms: () -> Unit = {},
    onPrivacy: () -> Unit = {},
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.spacing.huge)
            .padding(top = MaterialTheme.spacing.huge, bottom = MaterialTheme.spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(MaterialTheme.spacing.xxl))
        AppMark()
        Spacer(Modifier.height(MaterialTheme.spacing.xxl))
        Text("APOGEE", style = PactType.eyebrow, color = MaterialTheme.semantic.accent)
        Spacer(Modifier.weight(1f))
        Text(
            "Become the\nbest version\nof yourself.",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        Text(
            "Build unbreakable habits, win every day, and grow through consistency, discipline & daily action.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
        if (isLoading) {
            Box(Modifier.fillMaxWidth().height(54.dp), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(color = MaterialTheme.semantic.accent)
            }
        } else {
            GoogleContinueButton(onClick = onContinueWithGoogle)
        }
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Text(
            "By continuing you agree to our",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Row(horizontalArrangement = Arrangement.Center) {
            Text(
                "Terms",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.semantic.accent,
                modifier = Modifier.clickable(onClick = onTerms),
            )
            Text(" & ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "Privacy Policy",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.semantic.accent,
                modifier = Modifier.clickable(onClick = onPrivacy),
            )
        }
    }
}

@Composable
private fun AppMark() {
    Box(contentAlignment = Alignment.Center) {
        // Soft radial glow + concentric rings behind the mark.
        Box(
            Modifier
                .size(220.dp)
                .background(
                    Brush.radialGradient(listOf(MaterialTheme.semantic.accent.copy(alpha = 0.30f), Color.Transparent)),
                    CircleShape,
                ),
        )
        Box(
            Modifier
                .size(180.dp)
                .border(1.dp, MaterialTheme.semantic.accent.copy(alpha = 0.12f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(PactShapeTokens.hero)
                .background(Brush.linearGradient(listOf(MaterialTheme.semantic.accentBright, MaterialTheme.semantic.accent, MaterialTheme.semantic.accentDeep))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Bolt, contentDescription = "Apogee", tint = Color(0xFF06121F), modifier = Modifier.size(48.dp))
        }
    }
}
