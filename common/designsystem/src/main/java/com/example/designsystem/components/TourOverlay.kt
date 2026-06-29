package com.example.designsystem.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.PactShapeTokens
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing
import kotlinx.coroutines.launch

/** One page of the first-run walkthrough. */
data class TourPage(val icon: ImageVector, val title: String, val lines: List<String>)

/**
 * Full-screen first-run walkthrough: a page per main screen explaining how to use it. Caller
 * persists completion and hides the overlay via [onFinish] (fired by Skip or the final button).
 */
@Composable
fun TourOverlay(pages: List<TourPage>, onFinish: () -> Unit, modifier: Modifier = Modifier) {
    if (pages.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.lastIndex

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        Column(Modifier.fillMaxSize().padding(MaterialTheme.spacing.screen)) {
            Row(Modifier.fillMaxWidth()) {
                Spacer(Modifier.weight(1f))
                Text(
                    "Skip",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(onClick = onFinish).padding(MaterialTheme.spacing.sm),
                )
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                PageContent(pages[page])
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                pages.indices.forEach { i ->
                    val selected = i == pagerState.currentPage
                    Box(
                        Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (selected) 9.dp else 7.dp)
                            .clip(CircleShape)
                            .background(if (selected) MaterialTheme.semantic.accent else MaterialTheme.semantic.hairlineStrong),
                    )
                }
            }
            Spacer(Modifier.height(MaterialTheme.spacing.xl))
            PactPrimaryButton(
                text = if (isLast) "Get started" else "Next",
                onClick = {
                    if (isLast) onFinish() else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
            )
        }
    }
}

@Composable
private fun PageContent(page: TourPage) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(PactShapeTokens.hero)
                .background(Brush.linearGradient(listOf(MaterialTheme.semantic.accentBright, MaterialTheme.semantic.accent, MaterialTheme.semantic.accentDeep))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(page.icon, contentDescription = null, tint = Color(0xFF06121F), modifier = Modifier.size(46.dp))
        }
        Spacer(Modifier.height(MaterialTheme.spacing.xxl))
        Text(page.title, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
        Spacer(Modifier.height(MaterialTheme.spacing.lg))
        page.lines.forEach { line ->
            Text(
                line,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.sm),
            )
        }
    }
}
