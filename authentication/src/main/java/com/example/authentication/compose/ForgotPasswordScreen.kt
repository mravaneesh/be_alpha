package com.example.authentication.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.designsystem.components.PactPrimaryButton
import com.example.designsystem.components.PactTextField
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing

/**
 * Forgot password. The fragment owns the Firestore username->email lookup and the Firebase reset
 * email; this screen collects the username and reflects the sent/loading state.
 */
@Composable
fun ForgotPasswordScreen(
    onSubmit: (username: String) -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean,
    sentMaskedEmail: String?,
    modifier: Modifier = Modifier,
) {
    var username by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.spacing.huge),
    ) {
        AuthTopBar(onBack)
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Text("Reset password", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Text(
            if (sentMaskedEmail == null) "Enter your username and we'll send a reset link to your email."
            else "Reset link sent to: $sentMaskedEmail",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xxl))

        if (sentMaskedEmail == null) {
            PactTextField(value = username, onValueChange = { username = it }, label = "Username", placeholder = "your_username")
            Spacer(Modifier.height(MaterialTheme.spacing.xxl))
            if (isLoading) {
                Box(Modifier.fillMaxWidth().heightIn(min = 52.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.semantic.accent)
                }
            } else {
                PactPrimaryButton(text = "Continue", onClick = { onSubmit(username.trim()) })
            }
        } else {
            PactPrimaryButton(text = "Done", onClick = onDone)
        }
    }
}
