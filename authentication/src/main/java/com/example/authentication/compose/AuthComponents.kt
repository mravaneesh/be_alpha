package com.example.authentication.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing

/** Full-width white "Continue with Google" CTA — the single entry point on the welcome screen. */
@Composable
fun GoogleContinueButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = 54.dp),
        shape = MaterialTheme.shapes.medium,
        color = androidx.compose.ui.graphics.Color.White,
        border = BorderStroke(1.dp, MaterialTheme.semantic.hairlineStrong),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "G",
                style = MaterialTheme.typography.titleMedium,
                color = androidx.compose.ui.graphics.Color(0xFF4285F4),
            )
            Spacer(Modifier.size(MaterialTheme.spacing.md))
            Text(
                "Continue with Google",
                style = MaterialTheme.typography.labelLarge,
                color = androidx.compose.ui.graphics.Color(0xFF1F1F1F),
            )
        }
    }
}

@Composable
fun AuthTopBar(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

/** "or continue with" divider + Google / Apple buttons. */
@Composable
fun SocialRow(onSocial: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Divider(Modifier.weight(1f))
        Text(
            "  or continue with  ",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Divider(Modifier.weight(1f))
    }
    Spacer(Modifier.size(MaterialTheme.spacing.md))
    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
        SocialButton("Google", Modifier.weight(1f)) { onSocial("Google") }
        SocialButton("Apple", Modifier.weight(1f)) { onSocial("Apple") }
    }
}

@Composable
private fun SocialButton(label: String, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 50.dp),
        border = BorderStroke(1.dp, MaterialTheme.semantic.hairlineStrong),
        shape = MaterialTheme.shapes.small,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun Divider(modifier: Modifier) {
    Box(
        modifier
            .height(1.dp)
            .background(MaterialTheme.semantic.hairlineStrong)
    )
}

/** "Already have an account? Sign in" style footer with a tappable accent action. */
@Composable
fun AuthFooterLink(prompt: String, action: String, onClick: () -> Unit) {
    val accent = MaterialTheme.semantic.accent
    val text = buildAnnotatedString {
        append("$prompt ")
        withStyle(SpanStyle(color = accent)) { append(action) }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.clickable(onClick = onClick),
    )
}
