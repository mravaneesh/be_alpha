package com.example.home_ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ai_domain.model.AiSuggestion
import com.example.designsystem.components.EmptyState
import com.example.designsystem.components.LoadingState
import com.example.designsystem.components.PactCard
import com.example.designsystem.components.XpBar
import com.example.designsystem.theme.PactType
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing
import com.example.home_domain.model.Post

/**
 * Social feed. Pure UI: state + actions are owned by FeedFragment so the existing Firestore
 * like-toggle, habit-progress query, and AI suggestion logic are untouched.
 */
@Composable
fun FeedScreen(
    posts: List<Post>,
    isLoading: Boolean,
    currentUserId: String,
    onLike: (Post) -> Unit,
    onComment: (Post) -> Unit,
    suggestion: AiSuggestion?,
    showHabitCard: Boolean,
    habitPercent: Int,
    onCloseHabitCard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        isLoading && posts.isEmpty() -> LoadingState(modifier)
        posts.isEmpty() && suggestion == null && !showHabitCard -> EmptyState(
            icon = Icons.Outlined.Inbox,
            title = "Your feed is quiet",
            message = "Follow people or share your first habit to fill it up.",
            modifier = modifier,
        )
        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(MaterialTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            if (showHabitCard) {
                item(key = "habit-progress") {
                    HabitProgressCard(percent = habitPercent, onClose = onCloseHabitCard)
                }
            }
            if (suggestion != null) {
                item(key = "ai-coach") { AiCoachCard(suggestion) }
            }
            items(posts, key = { it.id }) { post ->
                PostCard(
                    post = post,
                    isLiked = post.likes.contains(currentUserId),
                    onLike = { onLike(post) },
                    onComment = { onComment(post) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun HabitProgressCard(percent: Int, onClose: () -> Unit) {
    PactCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Today's habits", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text("$percent%", style = PactType.statMedium, color = MaterialTheme.semantic.accent)
            IconButton(onClick = onClose) {
                Icon(Icons.Outlined.Close, contentDescription = "Dismiss habit progress")
            }
        }
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        XpBar(progress = percent / 100f, height = 9.dp)
    }
}

@Composable
private fun AiCoachCard(suggestion: AiSuggestion) {
    PactCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.width(MaterialTheme.spacing.xs))
            Text("Your coach", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
        }
        if (suggestion.coachMessage.isNotBlank()) {
            Spacer(Modifier.height(MaterialTheme.spacing.xs))
            Text(suggestion.coachMessage, style = MaterialTheme.typography.bodyMedium)
        }
        if (suggestion.progressFeedback.isNotBlank()) {
            Spacer(Modifier.height(MaterialTheme.spacing.xs))
            Text(suggestion.progressFeedback, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (suggestion.tipOfTheDay.isNotBlank()) {
            Spacer(Modifier.height(MaterialTheme.spacing.xs))
            Text("💡 ${suggestion.tipOfTheDay}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PostCard(
    post: Post,
    isLiked: Boolean,
    onLike: () -> Unit,
    onComment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PactCard(modifier = modifier.fillMaxWidth()) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = post.userName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Spacer(Modifier.width(MaterialTheme.spacing.sm))
            Column {
                Text(post.userName.ifBlank { "Someone" }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (post.habitTitle.isNotBlank()) {
                    Text(post.habitTitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (post.imageUrls.isNotEmpty()) {
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            PostImages(post.imageUrls)
        }

        if (post.caption.isNotBlank()) {
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            Text(post.caption, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(MaterialTheme.spacing.xs))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LikeButton(isLiked = isLiked, count = post.likes.size, onLike = onLike)
            Spacer(Modifier.width(MaterialTheme.spacing.md))
            IconButton(onClick = onComment) {
                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comment")
            }
        }
    }
}

@Composable
private fun LikeButton(isLiked: Boolean, count: Int, onLike: () -> Unit) {
    // Micro-interaction: heart pops when liked.
    val scale by animateFloatAsState(if (isLiked) 1.15f else 1f, label = "likeScale")
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onLike) {
            Icon(
                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isLiked) "Unlike" else "Like",
                tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale },
            )
        }
        if (count > 0) Text("$count", style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun PostImages(urls: List<String>) {
    val pagerState = rememberPagerState(pageCount = { urls.size })
    Column {
        HorizontalPager(state = pagerState) { page ->
            AsyncImage(
                model = urls[page],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.medium),
            )
        }
        if (urls.size > 1) {
            Spacer(Modifier.height(MaterialTheme.spacing.xs))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(urls.size) { i ->
                    val selected = pagerState.currentPage == i
                    Box(
                        Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (selected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline
                            )
                    )
                }
            }
        }
    }
}
