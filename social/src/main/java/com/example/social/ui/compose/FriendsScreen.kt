package com.example.social.ui.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.PlaylistAddCheck
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import com.example.designsystem.components.CoachmarkHost
import com.example.designsystem.components.CoachmarkStep
import com.example.designsystem.components.EmptyState
import com.example.designsystem.components.PactPrimaryButton
import com.example.designsystem.components.PactTextField
import com.example.designsystem.components.coachmarkTarget
import com.example.designsystem.components.ProgressRing
import com.example.designsystem.components.XpBar
import com.example.designsystem.theme.PactType
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing
import com.example.social.domain.model.Challenge
import com.example.social.domain.model.FriendRequest
import com.example.social.domain.model.FriendState
import com.example.social.domain.model.FriendSummary
import com.example.social.domain.model.LeaderboardEntry
import com.example.social.domain.model.Nudge
import com.example.social.domain.model.amInvited
import com.example.social.domain.model.amMember
import com.example.social.domain.model.dayIndex
import com.example.social.domain.model.daysLeft
import com.example.social.domain.model.doneToday
import com.example.social.domain.model.groupProgress
import com.example.social.domain.model.habits
import com.example.social.domain.model.isActive
import com.example.social.domain.model.leaderboard
import com.example.social.domain.model.myProgress
import com.example.social.domain.model.ownerName
import com.example.social.domain.model.UserSearchResult
import com.example.social.ui.viewmodel.SearchState

private enum class Route { HOME, ADD_FRIENDS, NOTIFICATIONS, CHALLENGES, CHALLENGE_DETAIL, CREATE_CHALLENGE }

/** A habit the user can link to a new challenge (id + display title), kept free of the goal domain type. */
data class HabitPick(val id: String, val title: String)

private val communityTourSteps = listOf(
    CoachmarkStep("addfriend", "Add friends", "Connect with friends and keep each other accountable."),
    CoachmarkStep("challenges", "Challenges", "Take on challenges together and stay motivated."),
    CoachmarkStep("friends", "Your friends", "See who you're building habits with and cheer them on."),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    feed: List<FriendSummary>,
    requests: List<FriendRequest>,
    nudgeCount: Int,
    search: SearchState,
    suggestions: List<UserSearchResult>,
    challenges: List<Challenge>,
    publicChallenges: List<Challenge>,
    recentNudges: List<Nudge>,
    habits: List<HabitPick>,
    myUid: String?,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    onLoadSuggestions: () -> Unit,
    onSendRequest: (String) -> Unit,
    onAccept: (FriendRequest) -> Unit,
    onDecline: (FriendRequest) -> Unit,
    onNudge: (String) -> Unit,
    onRemoveFriend: (String) -> Unit,
    onDismissNudges: () -> Unit,
    onInvite: () -> Unit,
    onCreateChallenge: (title: String, durationDays: Int, isPublic: Boolean, invited: List<FriendSummary>, newHabitNames: List<String>, existingHabitIds: List<String>) -> Unit,
    onLeaveChallenge: (String, Boolean) -> Unit,
    onAcceptChallenge: (String) -> Unit,
    onDeclineChallenge: (String) -> Unit,
    onJoinChallenge: (String) -> Unit,
    onMarkChallengeDone: (String) -> Unit,
    runTour: Boolean = false,
    onTourFinished: () -> Unit = {},
    onSubScreenChanged: (Boolean) -> Unit = {},
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    // Internal back-stack so Back / after-action returns to wherever you came from.
    val nav = remember { mutableStateListOf(Route.HOME) }
    val route = nav.last()
    var openFriend by remember { mutableStateOf<FriendSummary?>(null) }
    var openChallengeId by remember { mutableStateOf<String?>(null) }

    fun go(r: Route) { nav.add(r) }
    fun pop() { if (nav.size > 1) nav.removeAt(nav.lastIndex) }

    BackHandler(enabled = nav.size > 1) { pop() }

    // Tell the host to hide its bottom bar while on any sub-screen.
    LaunchedEffect(route) { onSubScreenChanged(route != Route.HOME) }

    Box(modifier.fillMaxSize()) {
      CoachmarkHost(steps = communityTourSteps, enabled = runTour && route == Route.HOME, onFinish = onTourFinished) {
        when (route) {
            Route.HOME -> CommunityHome(
                feed = feed,
                challenges = challenges,
                myUid = myUid,
                requestCount = requests.size,
                nudgeCount = nudgeCount,
                onAddFriends = { go(Route.ADD_FRIENDS) },
                onOpenNotifications = { go(Route.NOTIFICATIONS) },
                onOpenFriend = { openFriend = it },
                onSeeChallenges = { go(Route.CHALLENGES) },
                onOpenChallenge = { openChallengeId = it; go(Route.CHALLENGE_DETAIL) },
                onNewChallenge = { go(Route.CREATE_CHALLENGE) },
                onBack = onBack,
            )
            Route.ADD_FRIENDS -> AddFriendsRoute(
                requests = requests,
                search = search,
                suggestions = suggestions,
                onSearch = onSearch,
                onClearSearch = onClearSearch,
                onLoadSuggestions = onLoadSuggestions,
                onSendRequest = onSendRequest,
                onAccept = onAccept,
                onDecline = onDecline,
                onInvite = onInvite,
                onBack = { pop() },
            )
            Route.NOTIFICATIONS -> NotificationsRoute(
                nudges = recentNudges,
                onSeen = onDismissNudges,
                onBack = { pop() },
            )
            Route.CHALLENGES -> ChallengesRoute(
                challenges = challenges,
                publicChallenges = publicChallenges.filter { !it.amMember(myUid) && !it.amInvited(myUid) },
                myUid = myUid,
                onOpenChallenge = { openChallengeId = it; go(Route.CHALLENGE_DETAIL) },
                onNewChallenge = { go(Route.CREATE_CHALLENGE) },
                onBack = { pop() },
            )
            Route.CHALLENGE_DETAIL -> ChallengeDetailRoute(
                challenge = challenges.firstOrNull { it.id == openChallengeId }
                    ?: publicChallenges.firstOrNull { it.id == openChallengeId },
                myUid = myUid,
                onLeave = { id, deleteHabit -> onLeaveChallenge(id, deleteHabit); pop() },
                onAccept = { id -> onAcceptChallenge(id); pop() },
                onDecline = { id -> onDeclineChallenge(id); pop() },
                onJoin = { id -> onJoinChallenge(id); pop() },
                onMarkDone = onMarkChallengeDone,
                onBack = { pop() },
            )
            Route.CREATE_CHALLENGE -> CreateChallengeRoute(
                friends = feed,
                habits = habits,
                onCreate = { title, days, isPublic, invited, newHabitNames, existingHabitIds ->
                    onCreateChallenge(title, days, isPublic, invited, newHabitNames, existingHabitIds)
                    pop()
                },
                onBack = { pop() },
            )
        }
      }
    }

    openFriend?.let { friend ->
        ModalBottomSheet(
            onDismissRequest = { openFriend = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            FriendProfileContent(
                friend = friend,
                onCheer = { onNudge(friend.uid) },
                onRemove = { onRemoveFriend(friend.uid); openFriend = null },
            )
        }
    }
}

// ---------------------------------------------------------------- Home

@Composable
private fun CommunityHome(
    feed: List<FriendSummary>,
    challenges: List<Challenge>,
    myUid: String?,
    requestCount: Int,
    nudgeCount: Int,
    onAddFriends: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenFriend: (FriendSummary) -> Unit,
    onSeeChallenges: () -> Unit,
    onOpenChallenge: (String) -> Unit,
    onNewChallenge: () -> Unit,
    onBack: (() -> Unit)?,
) {
    val friendCount = feed.size
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    LazyColumn(
        contentPadding = PaddingValues(
            start = MaterialTheme.spacing.screen,
            end = MaterialTheme.spacing.screen,
            top = topInset + MaterialTheme.spacing.sm,
            bottom = MaterialTheme.spacing.screen + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text("Community", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        when (friendCount) {
                            0 -> "Add friends to take on challenges together"
                            1 -> "1 friend · take on challenges together"
                            else -> "$friendCount friends · take on challenges together"
                        },
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                BadgeIconButton(icon = Icons.Outlined.Notifications, count = nudgeCount, onClick = onOpenNotifications)
                Spacer(Modifier.width(MaterialTheme.spacing.sm))
                Box(Modifier.coachmarkTarget("addfriend")) {
                    BadgeIconButton(icon = Icons.Filled.PersonAdd, count = requestCount, onClick = onAddFriends)
                }
            }
        }

        item {
            // Always offer a way into the full Challenges screen — "Browse" when you have none yet
            // (so new users can discover public challenges), "See all" once you've joined some.
            SectionHeader("Challenges", actionLabel = if (challenges.isEmpty()) "Browse" else "See all", onAction = onSeeChallenges)
        }
        item { Box(Modifier.coachmarkTarget("challenges")) { ChallengesStrip(challenges, myUid, onOpen = onOpenChallenge, onNew = onNewChallenge) } }

        item { Box(Modifier.coachmarkTarget("friends")) { SectionHeader("Friends") } }

        if (feed.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.PersonAddAlt,
                    title = "No friends yet",
                    message = "Add friends so you can invite them to challenges.",
                )
            }
        } else {
            items(feed, key = { it.uid }) { f ->
                Card(modifier = Modifier.clickable { onOpenFriend(f) }) {
                    Avatar(f.name, f.photoUrl, size = 40.dp)
                    Spacer(Modifier.width(MaterialTheme.spacing.md))
                    Column(Modifier.weight(1f)) {
                        Text(f.name.ifBlank { "Friend" }, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                        Text("@${f.username}", style = PactType.mono, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private val ChallengeStripHeight = 108.dp

/** Horizontal strip on the home: compact challenge cards + a "New" card. */
@Composable
private fun ChallengesStrip(challenges: List<Challenge>, myUid: String?, onOpen: (String) -> Unit, onNew: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        challenges.forEach { c -> ChallengeCompactCard(c, invited = c.amInvited(myUid), onClick = { onOpen(c.id) }) }
        Column(
            modifier = Modifier
                .width(120.dp).height(ChallengeStripHeight)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surface)
                .border(BorderStroke(1.dp, MaterialTheme.semantic.hairline), MaterialTheme.shapes.large)
                .clickable(onClick = onNew),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.semantic.accent.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = MaterialTheme.semantic.accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            Text("New", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ChallengeCompactCard(c: Challenge, invited: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(190.dp)
            .height(ChallengeStripHeight)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.dp, if (invited) MaterialTheme.semantic.accent.copy(alpha = 0.4f) else MaterialTheme.semantic.hairline), MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .padding(MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ChallengeRing(c.groupProgress(), 42.dp)
            Spacer(Modifier.width(MaterialTheme.spacing.md))
            Column {
                Text(c.title.ifBlank { "Challenge" }, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Text("Day ${c.dayIndex()}/${c.durationDays}", style = PactType.mono.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (invited) {
            Text("Invited · tap to respond", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.semantic.accent, maxLines = 1)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.semantic.accent, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                Text("${c.daysLeft()}d left · ${c.memberUids.size} in", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

/** Ring with the challenge trophy in the centre. */
@Composable
private fun ChallengeRing(progress: Float, size: Dp) {
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        ProgressRing(progress = progress, strokeWidth = 5.dp, modifier = Modifier.size(size))
        Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = MaterialTheme.semantic.accent, modifier = Modifier.size(size * 0.4f))
    }
}

// ---------------------------------------------------------------- Challenges list

@Composable
private fun ChallengesRoute(
    challenges: List<Challenge>,
    publicChallenges: List<Challenge>,
    myUid: String?,
    onOpenChallenge: (String) -> Unit,
    onNewChallenge: () -> Unit,
    onBack: () -> Unit,
) {
    // First-timers (no challenges of their own) land on Browse to discover public ones.
    var browse by remember { mutableStateOf(challenges.isEmpty()) }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = MaterialTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface) }
            Text("Challenges", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
            BadgeIconButton(icon = Icons.Filled.PersonAdd, count = 0, onClick = onNewChallenge)
            Spacer(Modifier.width(MaterialTheme.spacing.sm))
        }
        LazyColumn(
            contentPadding = PaddingValues(
                start = MaterialTheme.spacing.screen, end = MaterialTheme.spacing.screen,
                top = MaterialTheme.spacing.sm, bottom = MaterialTheme.spacing.screen + 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            item {
                SegmentedToggle(listOf("Mine", "Browse"), if (browse) 1 else 0) { browse = it == 1 }
            }
            if (browse) {
                if (publicChallenges.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Outlined.EmojiEvents,
                            title = "No public challenges",
                            message = "Be the first — create one and make it public so anyone can join.",
                        )
                    }
                } else {
                    items(publicChallenges, key = { it.id }) { c ->
                        ChallengeCard(c, invited = false, onClick = { onOpenChallenge(c.id) })
                    }
                }
            } else if (challenges.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.EmojiEvents,
                        title = "No challenges yet",
                        message = "Start a challenge and invite friends to do habits together.",
                    )
                }
            } else {
                items(challenges, key = { it.id }) { c -> ChallengeCard(c, invited = c.amInvited(myUid), onClick = { onOpenChallenge(c.id) }) }
            }
        }
    }
}

@Composable
private fun ChallengeCard(c: Challenge, invited: Boolean, onClick: () -> Unit) {
    Card(highlight = invited, modifier = Modifier.clickable(onClick = onClick)) {
        ChallengeRing(c.groupProgress(), 52.dp)
        Spacer(Modifier.width(MaterialTheme.spacing.md))
        Column(Modifier.weight(1f)) {
            Text(c.title.ifBlank { "Challenge" }, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            Text(
                if (invited) "${c.ownerName()} invited you · ${c.durationDays} days"
                else "Day ${c.dayIndex()} of ${c.durationDays} · ${(c.groupProgress() * 100).toInt()}% group",
                style = PactType.mono.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize), color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val pillColor = if (invited) MaterialTheme.semantic.success else MaterialTheme.semantic.accent
        Row(
            modifier = Modifier.clip(MaterialTheme.shapes.large).background(pillColor.copy(alpha = 0.14f)).padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (invited) {
                Text("Invited", style = MaterialTheme.typography.labelMedium, color = pillColor)
            } else {
                Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = pillColor, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                Text("${c.daysLeft()}d", style = MaterialTheme.typography.labelMedium, color = pillColor)
            }
        }
    }
}

// ---------------------------------------------------------------- Challenge detail

@Composable
private fun ChallengeDetailRoute(
    challenge: Challenge?,
    myUid: String?,
    onLeave: (String, Boolean) -> Unit,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    onJoin: (String) -> Unit,
    onMarkDone: (String) -> Unit,
    onBack: () -> Unit,
) {
    if (challenge == null) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            SubHeader("Challenge", onBack, padded = true)
            EmptyState(icon = Icons.Outlined.EmojiEvents, title = "Not found", message = "This challenge is no longer available.")
        }
        return
    }
    val invited = challenge.amInvited(myUid)
    val member = challenge.amMember(myUid)
    val iDidToday = challenge.members.firstOrNull { it.uid == myUid }?.doneToday() == true
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var confirmLeave by remember { mutableStateOf(false) }
    if (confirmLeave) {
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            title = { Text("Leave challenge?") },
            text = { Text("You'll stop counting toward \"${challenge.title.ifBlank { "this challenge" }}\". Keep its habit in your Habits tab, or delete it too?") },
            confirmButton = {
                TextButton(onClick = { confirmLeave = false; onLeave(challenge.id, false) }) {
                    Text("Keep habit & leave")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmLeave = false; onLeave(challenge.id, true) }) {
                    Text("Delete habit too", color = MaterialTheme.semantic.urgent)
                }
            },
        )
    }
    LazyColumn(
        contentPadding = PaddingValues(
            start = MaterialTheme.spacing.screen, end = MaterialTheme.spacing.screen,
            top = topInset + MaterialTheme.spacing.sm, bottom = MaterialTheme.spacing.screen + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        item { SubHeader(challenge.title.ifBlank { "Challenge" }, onBack) }
        if (invited) {
            item {
                Row(
                    Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.semantic.accent.copy(alpha = 0.12f)).padding(MaterialTheme.spacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = MaterialTheme.semantic.accent, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(MaterialTheme.spacing.md))
                    Text("${challenge.ownerName()} invited you to this challenge", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        item {
            val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
            val heroGradient = if (dark) listOf(Color(0xFF11141A), Color(0xFF0B0C10))
            else listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant)
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                    .background(Brush.verticalGradient(heroGradient))
                    .border(BorderStroke(1.dp, MaterialTheme.semantic.hairline), RoundedCornerShape(28.dp))
                    .padding(MaterialTheme.spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("GROUP PROGRESS", style = PactType.eyebrow, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(MaterialTheme.spacing.md))
                Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
                    ProgressRing(progress = challenge.groupProgress(), strokeWidth = 11.dp, modifier = Modifier.size(150.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${(challenge.groupProgress() * 100).toInt()}%", style = PactType.statLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text("Day ${challenge.dayIndex()}/${challenge.durationDays}", style = PactType.eyebrow, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(MaterialTheme.spacing.lg))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HeroStat("${challenge.myProgress(myUid)}", "You", MaterialTheme.semantic.success, Modifier.weight(1f))
                    HeroDivider()
                    HeroStat("${challenge.memberUids.size}", "Members", MaterialTheme.colorScheme.onSurface, Modifier.weight(1f))
                    HeroDivider()
                    HeroStat("${challenge.daysLeft()}", "Days left", MaterialTheme.semantic.streak, Modifier.weight(1f))
                }
            }
        }
        if (challenge.habits().isNotEmpty()) {
            item { SectionHeader("Habits") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                    challenge.habits().forEach { name ->
                        Row(
                            Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(BorderStroke(1.dp, MaterialTheme.semantic.hairline), MaterialTheme.shapes.large)
                                .padding(MaterialTheme.spacing.lg),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Outlined.PlaylistAddCheck, contentDescription = null, tint = MaterialTheme.semantic.accent, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(MaterialTheme.spacing.md))
                            Text(name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                        }
                    }
                }
            }
        }
        item { SectionHeader("Members") }
        items(challenge.leaderboard(), key = { it.uid }) { m ->
            val isMe = m.uid == myUid
            Card(highlight = isMe) {
                val rank = challenge.leaderboard().indexOfFirst { it.uid == m.uid } + 1
                Text("#$rank", style = PactType.mono, color = if (isMe) MaterialTheme.semantic.accent else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(30.dp))
                Avatar(m.name, m.photoUrl, size = 38.dp)
                Spacer(Modifier.width(MaterialTheme.spacing.md))
                Text(if (isMe) "You" else m.name.ifBlank { "Friend" }, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, modifier = Modifier.weight(1f))
                if (m.doneToday()) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Done today", tint = MaterialTheme.semantic.success, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(MaterialTheme.spacing.sm))
                }
                Text("${m.progress}", style = PactType.statMedium, color = MaterialTheme.semantic.success)
            }
        }
        item {
            Spacer(Modifier.height(MaterialTheme.spacing.md))
            when {
                invited -> Row(
                    Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
                ) {
                    Box(Modifier.weight(2f)) { PactPrimaryButton(text = "Accept", onClick = { onAccept(challenge.id) }) }
                    Box(
                        Modifier.weight(1f).fillMaxHeight().clip(MaterialTheme.shapes.large)
                            .border(BorderStroke(1.dp, MaterialTheme.semantic.hairlineStrong), MaterialTheme.shapes.large)
                            .clickable { onDecline(challenge.id) },
                        contentAlignment = Alignment.Center,
                    ) { Text("Decline", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.semantic.urgent) }
                }
                member -> Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
                    if (challenge.isActive()) {
                        if (iDidToday) {
                            Row(
                                Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large)
                                    .background(MaterialTheme.semantic.success.copy(alpha = 0.14f))
                                    .padding(vertical = MaterialTheme.spacing.lg),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.semantic.success, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(MaterialTheme.spacing.sm))
                                Text("Checked in today", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.semantic.success)
                            }
                        } else {
                            PactPrimaryButton(text = "Mark today done", onClick = { onMarkDone(challenge.id) })
                        }
                    }
                    Box(
                        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large)
                            .border(BorderStroke(1.dp, MaterialTheme.semantic.hairlineStrong), MaterialTheme.shapes.large)
                            .clickable { confirmLeave = true }.padding(vertical = MaterialTheme.spacing.lg),
                        contentAlignment = Alignment.Center,
                    ) { Text("Leave challenge", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.semantic.urgent) }
                }
                else -> PactPrimaryButton(text = "Join challenge", onClick = { onJoin(challenge.id) })
            }
        }
    }
}

// ---------------------------------------------------------------- Create challenge

@Composable
private fun CreateChallengeRoute(
    friends: List<FriendSummary>,
    habits: List<HabitPick>,
    onCreate: (title: String, durationDays: Int, isPublic: Boolean, invited: List<FriendSummary>, newHabitNames: List<String>, existingHabitIds: List<String>) -> Unit,
    onBack: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var duration by remember { mutableIntStateOf(30) }
    var isPublic by remember { mutableStateOf(false) }
    val selected = remember { mutableStateListOf<String>() }
    // The challenge's habits: freshly-typed names + existing habits the user links.
    val newNames = remember { mutableStateListOf<String>() }
    val pickedExisting = remember { mutableStateListOf<String>() }
    var habitInput by remember { mutableStateOf("") }
    val hasHabit = newNames.isNotEmpty() || pickedExisting.isNotEmpty()
    fun addHabit() {
        val n = habitInput.trim()
        if (n.isNotBlank() && newNames.none { it.equals(n, ignoreCase = true) }) newNames.add(n)
        habitInput = ""
    }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        SubHeader("New challenge", onBack, padded = true)
        LazyColumn(
            contentPadding = PaddingValues(
                start = MaterialTheme.spacing.screen, end = MaterialTheme.spacing.screen,
                top = MaterialTheme.spacing.sm, bottom = MaterialTheme.spacing.screen + 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            item {
                Text("NAME", style = PactType.eyebrow, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                PactTextField(value = title, onValueChange = { title = it }, label = "Challenge name", placeholder = "e.g. 30 Days of Reading")
            }
            item {
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                Text("HABITS", style = PactType.eyebrow, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                Text(
                    "Add one or more habits. Completing all of them each day is your check-in.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PactTextField(
                        modifier = Modifier.weight(1f),
                        value = habitInput,
                        onValueChange = { habitInput = it },
                        label = "",
                        placeholder = "e.g. Read 20 min",
                    )
                    Spacer(Modifier.width(MaterialTheme.spacing.sm))
                    Box(
                        modifier = Modifier
                            .size(56.dp).padding(3.dp).clip(MaterialTheme.shapes.small)
                            .background(Brush.linearGradient(listOf(MaterialTheme.semantic.accentBright, MaterialTheme.semantic.accent)))
                            .clickable(enabled = habitInput.isNotBlank()) { addHabit() },
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Filled.Add, contentDescription = "Add habit", tint = Color(0xFF06121F)) }
                }
            }
            items(newNames, key = { "new:$it" }) { name ->
                Card {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.semantic.accent, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(MaterialTheme.spacing.md))
                    Text(name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, modifier = Modifier.weight(1f))
                    Text("Remove", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.semantic.urgent, modifier = Modifier.clickable { newNames.remove(name) })
                }
            }
            if (habits.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(MaterialTheme.spacing.sm))
                    Text("Or add from your habits", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(habits, key = { it.id }) { h ->
                    val on = pickedExisting.contains(h.id)
                    Card(modifier = Modifier.clickable { if (on) pickedExisting.remove(h.id) else pickedExisting.add(h.id) }) {
                        Text(h.title.ifBlank { "Habit" }, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, modifier = Modifier.weight(1f))
                        Icon(
                            if (on) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = null,
                            tint = if (on) MaterialTheme.semantic.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                Text("DURATION", style = PactType.eyebrow, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                    listOf(7, 21, 30).forEach { d ->
                        DurationPill("$d days", selected = duration == d) { duration = d }
                    }
                }
            }
            item {
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                Text("VISIBILITY", style = PactType.eyebrow, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                    DurationPill("Private", selected = !isPublic) { isPublic = false }
                    DurationPill("Public", selected = isPublic) { isPublic = true }
                }
                Text(
                    if (isPublic) "Anyone can find and join this challenge." else "Only friends you invite can join.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
                )
            }
            item {
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                Text(if (isPublic) "INVITE FRIENDS (OPTIONAL)" else "INVITE FRIENDS", style = PactType.eyebrow, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
            }
            if (friends.isEmpty()) {
                item { Text("Add friends first to invite them.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(friends, key = { it.uid }) { f ->
                    val on = selected.contains(f.uid)
                    Card(modifier = Modifier.clickable { if (on) selected.remove(f.uid) else selected.add(f.uid) }) {
                        Avatar(f.name, f.photoUrl, size = 38.dp)
                        Spacer(Modifier.width(MaterialTheme.spacing.md))
                        Text(f.name.ifBlank { "Friend" }, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, modifier = Modifier.weight(1f))
                        Icon(
                            if (on) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = null,
                            tint = if (on) MaterialTheme.semantic.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(MaterialTheme.spacing.md))
                PactPrimaryButton(
                    text = "Create challenge",
                    enabled = title.isNotBlank() && hasHabit,
                    onClick = {
                        onCreate(
                            title.trim(), duration, isPublic,
                            friends.filter { selected.contains(it.uid) },
                            newNames.toList(),
                            pickedExisting.toList(),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun DurationPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(if (selected) MaterialTheme.semantic.accent.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.dp, if (selected) MaterialTheme.semantic.accent.copy(alpha = 0.4f) else MaterialTheme.semantic.hairlineStrong), MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = MaterialTheme.spacing.lg, vertical = MaterialTheme.spacing.md),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = if (selected) MaterialTheme.semantic.accent else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HeroStat(value: String, label: String, color: Color, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = PactType.statMedium.copy(fontSize = 20.sp, lineHeight = 24.sp), color = color)
        Spacer(Modifier.height(3.dp))
        Text(label.uppercase(), style = PactType.eyebrow, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun HeroDivider() {
    Box(Modifier.padding(horizontal = 4.dp).width(1.dp).height(34.dp).background(MaterialTheme.semantic.hairlineStrong))
}

/** Pill row of mutually-exclusive segments (e.g. Mine / Browse). */
@Composable
private fun SegmentedToggle(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.dp, MaterialTheme.semantic.hairlineStrong), MaterialTheme.shapes.large)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(if (on) MaterialTheme.semantic.accent.copy(alpha = 0.16f) else Color.Transparent)
                    .clickable { onSelect(i) }
                    .padding(vertical = MaterialTheme.spacing.sm),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge, color = if (on) MaterialTheme.semantic.accent else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ---------------------------------------------------------------- Friend profile sheet

@Composable
private fun FriendProfileContent(friend: FriendSummary, onCheer: () -> Unit, onRemove: () -> Unit) {
    var confirmRemove by remember { mutableStateOf(false) }
    if (confirmRemove) {
        AlertDialog(
            onDismissRequest = { confirmRemove = false },
            title = { Text("Remove friend?") },
            text = { Text("${friend.name.ifBlank { "This friend" }} will be removed from your friends. You can add each other again later.") },
            confirmButton = {
                TextButton(onClick = { confirmRemove = false; onRemove() }) {
                    Text("Remove", color = MaterialTheme.semantic.urgent)
                }
            },
            dismissButton = { TextButton(onClick = { confirmRemove = false }) { Text("Cancel") } },
        )
    }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.screen)
            .padding(bottom = MaterialTheme.spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Avatar(friend.name, friend.photoUrl, size = 72.dp)
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Text(friend.name.ifBlank { "Friend" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
        Text("@${friend.username}", style = PactType.mono, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        Row(
            Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            Box(Modifier.weight(2f)) { PactPrimaryButton(text = "Cheer", onClick = onCheer) }
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.large)
                    .border(BorderStroke(1.dp, MaterialTheme.semantic.hairlineStrong), MaterialTheme.shapes.large)
                    .clickable { confirmRemove = true },
                contentAlignment = Alignment.Center,
            ) {
                Text("Remove", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.semantic.urgent)
            }
        }
    }
}


// ---------------------------------------------------------------- Add friends

@Composable
private fun AddFriendsRoute(
    requests: List<FriendRequest>,
    search: SearchState,
    suggestions: List<UserSearchResult>,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    onLoadSuggestions: () -> Unit,
    onSendRequest: (String) -> Unit,
    onAccept: (FriendRequest) -> Unit,
    onDecline: (FriendRequest) -> Unit,
    onInvite: () -> Unit,
    onBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        SubHeader("Add friends", onBack, padded = true)
        LazyColumn(
            contentPadding = PaddingValues(
                start = MaterialTheme.spacing.screen,
                end = MaterialTheme.spacing.screen,
                top = MaterialTheme.spacing.sm,
                bottom = MaterialTheme.spacing.screen + 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            item { FindTab(search, suggestions, onSearch, onClearSearch, onLoadSuggestions, onSendRequest) }

            if (requests.isNotEmpty()) {
                item { SectionHeader("Requests", trailing = "${requests.size}") }
                items(requests, key = { it.id }) { r ->
                    Card {
                        Avatar(r.fromName, "")
                        Spacer(Modifier.width(MaterialTheme.spacing.md))
                        Column(Modifier.weight(1f)) {
                            Text(r.fromName.ifBlank { "Someone" }, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                            Text("@${r.fromUsername}", style = PactType.mono, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                        RoundIcon(Icons.Filled.Check, MaterialTheme.semantic.success) { onAccept(r) }
                        Spacer(Modifier.width(MaterialTheme.spacing.sm))
                        RoundIcon(Icons.Filled.Close, MaterialTheme.semantic.urgent) { onDecline(r) }
                    }
                }
            }

            item { SectionHeader("Invite") }
            item {
                Card(modifier = Modifier.clickable(onClick = onInvite)) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.semantic.accent.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Outlined.Link, contentDescription = null, tint = MaterialTheme.semantic.accent, modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(MaterialTheme.spacing.md))
                    Column(Modifier.weight(1f)) {
                        Text("Share your invite link", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("Add friends not on Apogee yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun FindTab(
    search: SearchState,
    suggestions: List<UserSearchResult>,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    onLoadSuggestions: () -> Unit,
    onSendRequest: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }

    // Load the suggestion list once when this screen first appears.
    LaunchedEffect(Unit) { onLoadSuggestions() }

    // Debounced live search: fires after the user pauses typing; clears when empty.
    LaunchedEffect(query) {
        val q = query.trim()
        if (q.isBlank()) {
            onClearSearch()
        } else {
            delay(350)
            onSearch(q)
        }
    }

    Column(Modifier.fillMaxWidth()) {
        // No label on the field, so it's just the input box — the button matches its height exactly.
        Row(verticalAlignment = Alignment.CenterVertically) {
            PactTextField(
                modifier = Modifier.weight(1f),
                value = query,
                onValueChange = { query = it },
                label = "",
                placeholder = "name or username",
                leadingIcon = Icons.Filled.Search,
            )
            Spacer(Modifier.width(MaterialTheme.spacing.sm))
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .padding(3.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Brush.linearGradient(listOf(MaterialTheme.semantic.accentBright, MaterialTheme.semantic.accent)))
                    .clickable(enabled = query.isNotBlank()) { onSearch(query.trim()) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Search", tint = Color(0xFF06121F))
            }
        }
        Spacer(Modifier.height(MaterialTheme.spacing.md))

        val searching = query.isNotBlank()
        when {
            search.loading -> Box(Modifier.fillMaxWidth().padding(MaterialTheme.spacing.xl), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.semantic.accent)
            }
            searching && search.searched && search.results.isEmpty() ->
                Text("No one found. Try a different name or username.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            searching -> Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                search.results.forEach { r -> UserResultRow(r, onSendRequest) }
            }
            // Query empty → suggestions.
            suggestions.isEmpty() ->
                Text("No one to suggest yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            else -> Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                Text("SUGGESTED", style = PactType.eyebrow, color = MaterialTheme.colorScheme.onSurfaceVariant)
                suggestions.forEach { r -> UserResultRow(r, onSendRequest) }
            }
        }
    }
}

@Composable
private fun UserResultRow(r: UserSearchResult, onSendRequest: (String) -> Unit) {
    Card {
        Avatar(r.summary.name, r.summary.photoUrl)
        Spacer(Modifier.width(MaterialTheme.spacing.md))
        Column(Modifier.weight(1f)) {
            Text(r.summary.name.ifBlank { "User" }, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            Text("@${r.summary.username}", style = PactType.mono, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
        when (r.state) {
            FriendState.NONE -> AddButton { onSendRequest(r.summary.uid) }
            FriendState.PENDING -> Pill("Requested")
            FriendState.FRIENDS -> Pill("Friends")
            FriendState.SELF -> Pill("You")
        }
    }
}

// ---------------------------------------------------------------- Notifications

@Composable
private fun NotificationsRoute(nudges: List<Nudge>, onSeen: () -> Unit, onBack: () -> Unit) {
    // Opening the screen marks cheers seen, clearing the badge.
    LaunchedEffect(Unit) { onSeen() }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        SubHeader("Notifications", onBack, padded = true)
        if (nudges.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Notifications,
                title = "No notifications yet",
                message = "Cheers from your friends will show up here.",
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = MaterialTheme.spacing.screen, end = MaterialTheme.spacing.screen,
                    top = MaterialTheme.spacing.sm, bottom = MaterialTheme.spacing.screen + 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                items(nudges, key = { it.id }) { n ->
                    Card {
                        Box(
                            Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.semantic.accent.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Filled.Favorite, contentDescription = null, tint = MaterialTheme.semantic.accent, modifier = Modifier.size(18.dp)) }
                        Spacer(Modifier.width(MaterialTheme.spacing.md))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${n.fromName.ifBlank { "A friend" }} cheered you on 👏",
                                style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 2,
                            )
                            Text(relativeTime(n.createdAt), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

private fun relativeTime(epochMillis: Long): String {
    if (epochMillis <= 0L) return ""
    val minutes = (System.currentTimeMillis() - epochMillis) / 60_000
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 1440 -> "${minutes / 60}h ago"
        else -> "${minutes / 1440}d ago"
    }
}

// ---------------------------------------------------------------- shared bits

@Composable
private fun SectionHeader(title: String, actionLabel: String? = null, onAction: (() -> Unit)? = null, trailing: String? = null) {
    Row(
        Modifier.fillMaxWidth().padding(top = MaterialTheme.spacing.md, bottom = MaterialTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            Text(actionLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.semantic.accent, modifier = Modifier.clickable(onClick = onAction))
        }
        if (trailing != null) {
            Text(trailing, style = PactType.mono, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SubHeader(title: String, onBack: () -> Unit, padded: Boolean = false) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (padded) Modifier.padding(horizontal = MaterialTheme.spacing.sm) else Modifier)
            .padding(vertical = MaterialTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
        }
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun BadgeIconButton(icon: ImageVector, count: Int, onClick: () -> Unit) {
    Box {
        Box(
            Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(MaterialTheme.colorScheme.surface)
                .border(BorderStroke(1.dp, MaterialTheme.semantic.hairline), RoundedCornerShape(13.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, contentDescription = "Add friends", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp)) }
        if (count > 0) {
            Box(
                Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp).size(18.dp).clip(CircleShape).background(MaterialTheme.semantic.urgent),
                contentAlignment = Alignment.Center,
            ) { Text("$count", style = PactType.mono.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize), color = Color.White) }
        }
    }
}

@Composable
private fun Card(highlight: Boolean = false, modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(if (highlight) MaterialTheme.semantic.accent.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface)
            .border(
                BorderStroke(1.dp, if (highlight) MaterialTheme.semantic.accent.copy(alpha = 0.5f) else MaterialTheme.semantic.hairline),
                MaterialTheme.shapes.large,
            )
            .then(modifier)
            .padding(MaterialTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun Avatar(name: String, photoUrl: String, size: Dp = 44.dp) {
    if (photoUrl.isNotBlank()) {
        AsyncImage(
            model = photoUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size).clip(CircleShape),
        )
    } else {
        Box(
            modifier = Modifier.size(size).clip(CircleShape).background(MaterialTheme.semantic.accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(name.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.semantic.accent)
        }
    }
}

@Composable
private fun RoundIcon(icon: ImageVector, tint: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(40.dp).clip(CircleShape).background(tint.copy(alpha = 0.14f)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun AddButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.semantic.accent)
            .clickable(onClick = onClick)
            .padding(horizontal = MaterialTheme.spacing.lg, vertical = MaterialTheme.spacing.sm),
    ) {
        Text("Add", style = MaterialTheme.typography.labelLarge, color = Color(0xFF06121F))
    }
}

@Composable
private fun Pill(text: String) {
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .border(BorderStroke(1.dp, MaterialTheme.semantic.hairlineStrong), MaterialTheme.shapes.medium)
            .padding(horizontal = MaterialTheme.spacing.lg, vertical = MaterialTheme.spacing.sm),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
