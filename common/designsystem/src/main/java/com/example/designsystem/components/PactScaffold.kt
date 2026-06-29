package com.example.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Edge-to-edge aware scaffold. Content draws under transparent system bars; the [content]
 * lambda receives [PaddingValues] that already account for the bars plus any top/bottom bar,
 * so screens stay un-clipped without each one re-deriving insets.
 */
@Composable
fun PactScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        snackbarHost = snackbarHost,
        content = content,
    )
}

/** Convenience holder so callers don't import the Material type directly. */
typealias PactSnackbarState = SnackbarHostState
