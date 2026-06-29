package com.example.create_ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.create_ui.model.Challenge
import com.example.create_ui.model.SuggestedUser
import com.example.designsystem.components.LoadingState
import com.example.designsystem.components.PactCard
import com.example.designsystem.theme.spacing

@Composable
fun ExploreScreen(
    users: List<SuggestedUser>,
    usersLoading: Boolean,
    onFollow: (SuggestedUser) -> Unit,
    challenges: List<Challenge>,
    onChallengeClick: (Challenge) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(MaterialTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        item { SectionHeader("People to follow") }
        item {
            if (usersLoading) {
                Box(Modifier.fillMaxWidth().height(120.dp)) { LoadingState() }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                    items(users, key = { it.id }) { user -> SuggestedUserCard(user, onFollow) }
                }
            }
        }
        item { SectionHeader("Challenges") }
        items(challenges, key = { it.id }) { challenge ->
            ChallengeCard(challenge = challenge, onClick = { onChallengeClick(challenge) })
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun SuggestedUserCard(user: SuggestedUser, onFollow: (SuggestedUser) -> Unit) {
    PactCard(modifier = Modifier.width(150.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            if (user.profileImageUrl.isNotBlank()) {
                AsyncImage(
                    model = user.profileImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(56.dp).clip(CircleShape),
                )
            } else {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(56.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            user.name.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
            Text(
                user.name.ifBlank { user.username },
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = MaterialTheme.spacing.xs),
            )
            Text("@${user.username}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (user.isFollowing) {
                OutlinedButton(onClick = { onFollow(user) }, modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.spacing.xs)) {
                    Text("Following", style = MaterialTheme.typography.labelMedium)
                }
            } else {
                Button(
                    onClick = { onFollow(user) },
                    modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.spacing.xs),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Text("Follow", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(challenge: Challenge, onClick: () -> Unit) {
    PactCard(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(MaterialTheme.shapes.medium),
        ) {
            AsyncImage(
                model = challenge.bannerUrl,
                contentDescription = challenge.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(140.dp),
            )
        }
        Text(
            challenge.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = MaterialTheme.spacing.xs),
        )
        if (challenge.description.isNotBlank()) {
            Text(challenge.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
