package com.example.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.designsystem.components.EmptyState
import com.example.designsystem.components.PactCard
import com.example.designsystem.components.PactNavItem
import com.example.designsystem.components.PactPrimaryButton
import com.example.designsystem.components.PactScaffold
import com.example.designsystem.components.PactBottomBar
import com.example.designsystem.theme.PactTheme
import com.example.designsystem.theme.spacing

@Composable
private fun Gallery() {
    PactScaffold(
        bottomBar = {
            PactBottomBar(
                items = listOf(
                    PactNavItem(0, "Home", Icons.Outlined.Home),
                    PactNavItem(1, "Profile", Icons.Outlined.Person),
                ),
                selectedId = 0,
                onItemSelected = {},
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(MaterialTheme.spacing.md)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            Text("Display", style = MaterialTheme.typography.displayMedium)
            Text("Headline", style = MaterialTheme.typography.headlineSmall)
            Text("Body text in Inter.", style = MaterialTheme.typography.bodyMedium)
            PactCard {
                Text("Card title", style = MaterialTheme.typography.titleMedium)
                Text("Subtle, rounded, elevated.", style = MaterialTheme.typography.bodyMedium)
            }
            PactPrimaryButton(text = "Primary action", onClick = {})
            EmptyState(
                icon = Icons.Outlined.Inbox,
                title = "Nothing here yet",
                message = "Create your first habit to see it tracked here.",
                actionLabel = "Add habit",
                onAction = {},
            )
        }
    }
}

@Preview(name = "Design system - Light", showBackground = true)
@Composable
private fun GalleryLightPreview() {
    PactTheme(darkTheme = false, dynamicColor = false) {
        Surface { Gallery() }
    }
}

@Preview(name = "Design system - Dark", showBackground = true)
@Composable
private fun GalleryDarkPreview() {
    PactTheme(darkTheme = true, dynamicColor = false) {
        Surface { Gallery() }
    }
}
