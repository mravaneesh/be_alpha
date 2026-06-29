package com.example.bealpha_

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.bealpha_.databinding.ActivityHostBinding
import com.example.bealpha_.fcm.ApogeeMessagingService
import com.example.social.ui.view.CommunityNavHost
import com.example.designsystem.components.PactBottomBar
import com.example.designsystem.components.PactNavItem
import com.example.designsystem.components.TourOverlay
import com.example.designsystem.components.TourPage
import com.example.designsystem.theme.PactTheme
import com.example.utils.Prefs
import com.example.goal_domain.repository.GoalRepository
import com.example.goal_ui.viewmodel.GoalViewModel
import com.example.social.domain.repository.SocialRepository
import com.example.utils.reminder.HabitReminderScheduler
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HostActivity : AppCompatActivity(), CommunityNavHost {

    private lateinit var binding: ActivityHostBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private val isUserLoggedIn = FirebaseAuth.getInstance().currentUser != null

    @Inject lateinit var goalRepository: GoalRepository
    @Inject lateinit var socialRepository: SocialRepository

    // Shared with HabitFragment (activity-scoped) — used to focus a habit tapped from the widget.
    private val goalViewModel: GoalViewModel by viewModels()

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    // Bottom-nav tabs matching the current design scope.
    private val navItems = listOf(
        PactNavItem(com.example.home_ui.R.id.home_nav_graph, "Home", Icons.Outlined.Home, Icons.Filled.Home),
        PactNavItem(com.example.goal_ui.R.id.goal_nav_graph, "Habits", Icons.Outlined.CheckCircle, Icons.Filled.CheckCircle),
        PactNavItem(com.example.goal_ui.R.id.stats_nav_graph, "Stats", Icons.Outlined.Insights, Icons.Filled.Insights),
        PactNavItem(com.example.social.R.id.social_nav_graph, "Community", Icons.Outlined.Groups, Icons.Filled.Groups),
        PactNavItem(com.example.profile_ui.R.id.profile_nav_graph, "You", Icons.Outlined.Person, Icons.Filled.Person),
    )

    // Leaf destinations where the bottom bar should be visible.
    private val barVisibleDestinations = setOf(
        com.example.goal_ui.R.id.goalFragment,
        com.example.goal_ui.R.id.trackFragment,
        com.example.home_ui.R.id.homeFragment,
        com.example.social.R.id.friendsFragment,
        com.example.profile_ui.R.id.profileFragment
    )

    private var selectedNavId by mutableIntStateOf(navItems.first().id)
    private var communityBadge by mutableIntStateOf(0)
    private var tourShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Fully transparent system bars (no scrim) so content runs edge-to-edge beneath the
        // floating nav. Without this the framework paints a contrast scrim behind the nav bar on
        // 3-button-nav devices — a solid band that looked like a container under the pill.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        auth = FirebaseAuth.getInstance()
        binding = ActivityHostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.isNavigationBarContrastEnforced = false
        applyWindowInsets()
        setupNavigation(savedInstanceState)
        setupBottomBar()
        observeCommunityBadge()
        setupBackHandling()
        requestNotificationPermission()
        registerPushToken()
        rescheduleReminders()
        com.example.bealpha_.reminder.StreakRiskScheduler.scheduleDaily(this)
        handleFocusIntent(intent)
    }

    /** Community fragment hides the floating bar on its internal sub-screens, shows it on its home. */
    override fun onCommunitySubScreen(active: Boolean) {
        binding.bottomNavigation.visibility = if (active) View.GONE else View.VISIBLE
    }

    /** A widget habit tap launches us with apogee://focusHabit/<id> — open Habits and focus it. */
    private fun handleFocusIntent(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "apogee" && data.host == "focusHabit") {
            val id = data.lastPathSegment ?: return
            navigateToTab(com.example.goal_ui.R.id.goal_nav_graph)
            goalViewModel.focusHabit(id)
        }
    }

    /**
     * Back from a top-level tab exits the app (tabs don't stack on top of each other); back from a
     * drilled-in sub-screen pops normally.
     */
    private fun setupBackHandling() {
        onBackPressedDispatcher.addCallback(this) {
            if (binding.tourOverlay.visibility == View.VISIBLE) {
                Prefs.setTourCompleted(this@HostActivity, true)
                binding.tourOverlay.visibility = View.GONE
                binding.tourOverlay.setContent {}
                return@addCallback
            }
            val destId = navController.currentDestination?.id
            when {
                destId != null && barVisibleDestinations.contains(destId) -> finish()
                navController.popBackStack() -> Unit
                else -> finish()
            }
        }
    }

    /** Register this device's FCM token under the signed-in user so friends' cheers can push to it. */
    private fun registerPushToken() {
        if (!isUserLoggedIn) return
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token -> ApogeeMessagingService.saveToken(token) }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /** Re-arm reminders for every habit that has one (survives reinstall/clear; idempotent). */
    private fun rescheduleReminders() {
        val uid = auth.currentUser?.uid ?: return
        lifecycleScope.launch {
            runCatching { goalRepository.refreshGoals(uid, "Habit") }
            runCatching {
                goalRepository.observeGoals("Habit").first().forEach { goal ->
                    if (goal.reminder.isNotBlank()) {
                        HabitReminderScheduler.schedule(this@HostActivity, goal.id, goal.title, goal.reminder, goal.selectedDays)
                    }
                }
            }
        }
    }

    // Widget refresh is driven solely by BaseApplication's habit-cache observer, which fires AFTER
    // the Room write commits (race-free). Lifecycle refreshes here would race the write and show
    // the previous state ("one behind"), so they're intentionally omitted.

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (::navController.isInitialized) {
            navController.handleDeepLink(intent)
            handleFocusIntent(intent)
        }
    }

    private fun setupNavigation(savedInstanceState: Bundle?) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        if (savedInstanceState == null && !navController.handleDeepLink(intent)) {
            val navGraph = navController.navInflater.inflate(com.example.bealpha_.R.navigation.nav_graph)
            navGraph.setStartDestination(
                // Onboarding (AI user-profiling) is skipped for now — go straight to Home once authed.
                when {
                    !isUserLoggedIn -> com.example.authentication.R.id.auth_nav_graph
                    else -> com.example.home_ui.R.id.home_nav_graph
                }
            )
            navController.graph = navGraph
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigation.visibility =
                if (barVisibleDestinations.contains(destination.id)) View.VISIBLE else View.GONE
            // Highlight whichever top-level graph this destination lives under.
            val match = destination.hierarchy.firstOrNull { dest ->
                navItems.any { it.id == dest.id }
            }
            if (match != null) selectedNavId = match.id

            // Replaced by per-screen contextual walkthroughs (see CoachmarkHost on each screen).
            // The old full-screen welcome tour is disabled.
        }
    }

    private val tourPages = listOf(
        TourPage(Icons.Filled.Bolt, "Welcome to Apogee", listOf("Build better habits, one day at a time.", "Here's a quick tour.")),
        TourPage(Icons.Outlined.Home, "Home", listOf("Your day at a glance.", "See today's focus ring, progress and a daily quote.")),
        TourPage(Icons.Outlined.CheckCircle, "Habits", listOf("Tap + to add a habit.", "Tap the ring to mark it done — tap again to undo.", "Long-press for edit, share or delete; tap a habit for its history.")),
        TourPage(Icons.Outlined.Insights, "Stats", listOf("Track your trends and activity heatmap.", "Open 'Your week in review' for a weekly recap.")),
        TourPage(Icons.Outlined.Groups, "Community", listOf("Add friends and stay accountable.", "See their progress, climb the weekly ranks, and cheer each other on.")),
        TourPage(Icons.Outlined.Person, "You", listOf("Your level, streaks and achievements.", "Manage your profile and settings.")),
    )

    private fun showTour() {
        tourShown = true
        binding.tourOverlay.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            visibility = View.VISIBLE
            setContent {
                PactTheme {
                    TourOverlay(
                        pages = tourPages,
                        onFinish = {
                            Prefs.setTourCompleted(this@HostActivity, true)
                            binding.tourOverlay.visibility = View.GONE
                            binding.tourOverlay.setContent {}
                        },
                    )
                }
            }
        }
    }

    /** Badge the Community tab with unseen cheers + incoming friend requests. */
    private fun observeCommunityBadge() {
        if (!isUserLoggedIn) return
        lifecycleScope.launch {
            combine(
                socialRepository.observeUnseenNudges(),
                socialRepository.observeIncomingRequests(),
            ) { nudges, requests -> nudges.size + requests.size }
                .collect { communityBadge = it }
        }
    }

    private fun setupBottomBar() {
        binding.bottomNavigation.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PactTheme(applyBackground = false) {
                    PactBottomBar(
                        items = navItems.map {
                            if (it.id == com.example.social.R.id.social_nav_graph) it.copy(badgeCount = communityBadge) else it
                        },
                        selectedId = selectedNavId,
                        onItemSelected = { item -> navigateToTab(item.id) },
                    )
                }
            }
        }
    }

    /** Replicates BottomNavigationView + setupWithNavController tab-switch semantics. */
    private fun navigateToTab(itemId: Int) {
        if (itemId == selectedNavId) return
        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(true)
            .setPopUpTo(navController.graph.findStartDestination().id, inclusive = false, saveState = true)
            .build()
        runCatching { navController.navigate(itemId, null, options) }
    }

    private fun ComponentActivity.applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView.rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }
    }
}