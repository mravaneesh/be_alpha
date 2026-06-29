package com.example.profile_ui.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.designsystem.components.IconTile
import com.example.designsystem.theme.PactType
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing

/**
 * Settings — grouped preferences. Appearance follows the system theme (informational); the rest are
 * real actions wired by the fragment (notifications opens system settings, legal/share via intents,
 * logout + delete account in the account section).
 */
@Composable
fun SettingsScreen(
    email: String,
    appVersion: String,
    onNotifications: () -> Unit,
    onReplayWalkthroughs: () -> Unit,
    onTerms: () -> Unit,
    onPrivacy: () -> Unit,
    onShare: () -> Unit,
    onDeleteAccount: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmDelete by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.screen)
            .padding(bottom = MaterialTheme.spacing.xxl),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            Text("Settings", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(Modifier.height(MaterialTheme.spacing.lg))

        Section("ACCOUNT")
        Text(email, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = MaterialTheme.spacing.xs))

        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        Section("PREFERENCES")
        Group {
            PrefRow(Icons.Outlined.DarkMode, "Appearance", trailingText = "System")
            Divider()
            PrefRow(Icons.Outlined.Notifications, "Notifications", onClick = onNotifications)
            Divider()
            PrefRow(Icons.Outlined.Lightbulb, "Replay walkthroughs", onClick = onReplayWalkthroughs)
        }

        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        Section("ABOUT")
        Group {
            PrefRow(Icons.Outlined.Description, "Terms of Service", onClick = onTerms)
            Divider()
            PrefRow(Icons.Outlined.Shield, "Privacy Policy", onClick = onPrivacy)
            Divider()
            PrefRow(Icons.Outlined.IosShare, "Share Apogee", onClick = onShare)
        }

        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        DangerRow(Icons.AutoMirrored.Filled.Logout, "Log out", onClick = onLogout)
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        DangerRow(Icons.Outlined.Delete, "Delete account", onClick = { confirmDelete = true })

        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        Text(
            "Apogee v$appVersion",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete account?") },
            text = { Text("This permanently deletes your profile and habit data. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDeleteAccount() }) {
                    Text("Delete", color = MaterialTheme.semantic.urgent)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun Section(title: String) {
    Text(title, style = PactType.eyebrow, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(MaterialTheme.spacing.md))
}

@Composable
private fun Group(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.dp, MaterialTheme.semantic.hairline), MaterialTheme.shapes.large),
    ) { content() }
}

@Composable
private fun PrefRow(
    icon: ImageVector,
    label: String,
    trailingText: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(MaterialTheme.spacing.xl),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(icon = icon, tint = MaterialTheme.semantic.accent)
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Text("  $label", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        when {
            trailingText != null -> Text(trailingText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            onClick != null -> Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DangerRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.semantic.urgent.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(MaterialTheme.spacing.xl),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.semantic.urgent)
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Text("  $label", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.semantic.urgent)
    }
}

@Composable
private fun Divider() {
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = MaterialTheme.spacing.xl)
            .background(MaterialTheme.semantic.hairline),
    )
}
