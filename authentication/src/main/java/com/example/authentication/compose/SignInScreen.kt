package com.example.authentication.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.designsystem.components.PactPrimaryButton
import com.example.designsystem.components.PactTextField
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing

/**
 * Sign In. The fragment owns the Firestore username lookup + Firebase auth and the loading flag;
 * this screen just collects credentials and fires callbacks. The credential field maps to the
 * existing username lookup (not email), preserving the current login behavior.
 */
@Composable
fun SignInScreen(
    onSignIn: (username: String, password: String) -> Unit,
    onForgot: () -> Unit,
    onBack: () -> Unit,
    onCreateAccount: () -> Unit,
    onSocial: (String) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var keepSignedIn by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.huge)
            .padding(bottom = MaterialTheme.spacing.xxl),
    ) {
        AuthTopBar(onBack)
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Text("Welcome back", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Text(
            "Your streak is waiting. Let's keep it alive.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xxl))

        PactTextField(
            value = username,
            onValueChange = { username = it },
            label = "Username",
            placeholder = "your_username",
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xl))

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Password", style = com.example.designsystem.theme.PactType.eyebrow, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text("Forgot?", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.semantic.accent, modifier = Modifier.padding(end = 4.dp).clickable(onClick = onForgot))
        }
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        PactTextField(
            value = password,
            onValueChange = { password = it },
            label = "",
            placeholder = "••••••••",
            isPassword = true,
        )

        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = keepSignedIn, onCheckedChange = { keepSignedIn = it })
            Spacer(Modifier.height(0.dp))
            Text("  Keep me signed in", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        if (isLoading) {
            Box(Modifier.fillMaxWidth().heightIn(min = 52.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.semantic.accent)
            }
        } else {
            PactPrimaryButton(text = "Sign in", onClick = { onSignIn(username.trim(), password.trim()) })
        }

        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        SocialRow(onSocial)
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AuthFooterLink(prompt = "New to Apogee?", action = "Create account", onClick = onCreateAccount)
        }
    }
}
