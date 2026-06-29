package com.example.authentication.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.designsystem.components.PactPrimaryButton
import com.example.designsystem.components.PactTextField
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing

/**
 * Post-Google-sign-in profile setup. Name is prefilled from the Google account (editable); the
 * user picks a username that is checked for availability before [onContinue] can fire. The
 * fragment owns the debounced availability check and the Firestore write.
 */
@Composable
fun CompleteProfileScreen(
    initialName: String,
    initialUsername: String,
    onUsernameChange: (String) -> Unit,
    usernameAvailable: Boolean?,
    isLoading: Boolean,
    onContinue: (name: String, username: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var username by rememberSaveable { mutableStateOf(initialUsername) }

    // Validate the prefilled default once so Continue can be enabled immediately.
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (username.isNotBlank()) onUsernameChange(username)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.huge)
            .padding(top = MaterialTheme.spacing.huge, bottom = MaterialTheme.spacing.xxl),
    ) {
        Text("Set up your profile", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Text(
            "Pick a username. You can change your name anytime.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xxl))

        PactTextField(value = name, onValueChange = { name = it }, label = "Name", placeholder = "Your name")
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        PactTextField(
            value = username,
            onValueChange = { username = it.trim(); onUsernameChange(username) },
            label = "Username",
            placeholder = "your_username",
            trailing = { UsernameStatus(usernameAvailable) },
        )

        Spacer(Modifier.height(MaterialTheme.spacing.xxl))
        if (isLoading) {
            Box(Modifier.fillMaxWidth().heightIn(min = 52.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.semantic.accent)
            }
        } else {
            PactPrimaryButton(
                text = "Continue",
                enabled = name.isNotBlank() && username.isNotBlank() && usernameAvailable == true,
                onClick = { onContinue(name.trim(), username.trim()) },
            )
        }
    }
}

@Composable
private fun UsernameStatus(available: Boolean?) {
    when (available) {
        true -> StatusIcon(MaterialTheme.semantic.success, Icons.Filled.Check, "Username available")
        false -> StatusIcon(MaterialTheme.semantic.urgent, Icons.Filled.Close, "Username taken")
        null -> {}
    }
}

@Composable
private fun StatusIcon(
    color: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
) {
    Box(modifier = Modifier.size(20.dp).clip(CircleShape), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = desc, tint = color, modifier = Modifier.size(18.dp))
    }
}
