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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.designsystem.components.PactPrimaryButton
import com.example.designsystem.components.PactTextField
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing

/**
 * Sign Up. The fragment owns Firebase account creation, the debounced username-availability
 * check, the User-record write and onboarding navigation; this screen collects the fields and
 * reports the live username via [onUsernameChange], reflecting [usernameAvailable] as a trailing
 * indicator. A username field is kept (the design omits it, but the backend requires it).
 */
@Composable
fun SignUpScreen(
    onSignUp: (name: String, username: String, email: String, password: String) -> Unit,
    onUsernameChange: (String) -> Unit,
    usernameAvailable: Boolean?,
    onSignIn: () -> Unit,
    onBack: () -> Unit,
    onSocial: (String) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var agreed by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.huge)
            .padding(bottom = MaterialTheme.spacing.xxl),
    ) {
        AuthTopBar(onBack)
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Text("Create your account", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Text(
            "Start your streak today. It only takes a minute.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xxl))

        PactTextField(value = name, onValueChange = { name = it }, label = "Full name", placeholder = "Alex Mercer")
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        PactTextField(
            value = username,
            onValueChange = { username = it; onUsernameChange(it.trim()) },
            label = "Username",
            placeholder = "your_username",
            trailing = { UsernameStatus(available = usernameAvailable) },
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        PactTextField(value = email, onValueChange = { email = it }, label = "Email", placeholder = "alex@email.com", keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        PactTextField(value = password, onValueChange = { password = it }, label = "Password", placeholder = "••••••••", isPassword = true)

        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        TermsRow(agreed = agreed, onToggle = { agreed = it })

        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        if (isLoading) {
            Box(Modifier.fillMaxWidth().heightIn(min = 52.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.semantic.accent)
            }
        } else {
            PactPrimaryButton(
                text = "Create account",
                enabled = agreed,
                onClick = { onSignUp(name.trim(), username.trim(), email.trim(), password.trim()) },
            )
        }

        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        SocialRow(onSocial)
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AuthFooterLink(prompt = "Already have an account?", action = "Sign in", onClick = onSignIn)
        }
    }
}

@Composable
private fun UsernameStatus(available: Boolean?) {
    when (available) {
        true -> StatusDot(MaterialTheme.semantic.success, Icons.Filled.Check, "Username available")
        false -> StatusDot(MaterialTheme.semantic.urgent, Icons.Filled.Close, "Username taken")
        null -> {}
    }
}

@Composable
private fun StatusDot(color: androidx.compose.ui.graphics.Color, icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String) {
    Box(
        modifier = Modifier.size(20.dp).clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = desc, tint = color, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun TermsRow(agreed: Boolean, onToggle: (Boolean) -> Unit) {
    val accent = MaterialTheme.semantic.accent
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = agreed, onCheckedChange = onToggle)
        val text = buildAnnotatedString {
            append("I agree to the ")
            withStyle(SpanStyle(color = accent)) { append("Terms") }
            append(" & ")
            withStyle(SpanStyle(color = accent)) { append("Privacy Policy") }
            append(".")
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable { onToggle(!agreed) },
        )
    }
}
