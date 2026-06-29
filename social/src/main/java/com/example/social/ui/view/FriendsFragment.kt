package com.example.social.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.theme.PactTheme
import com.example.social.ui.compose.FriendsScreen
import com.example.social.ui.compose.HabitPick
import com.example.social.ui.viewmodel.ChallengesViewModel
import com.example.social.ui.viewmodel.FriendsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendsFragment : Fragment() {

    private val viewModel: FriendsViewModel by viewModels()
    private val challengesViewModel: ChallengesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PactTheme {
                    val feed by viewModel.feed.collectAsStateWithLifecycle()
                    val requests by viewModel.requests.collectAsStateWithLifecycle()
                    val nudges by viewModel.nudges.collectAsStateWithLifecycle()
                    val search by viewModel.search.collectAsStateWithLifecycle()
                    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
                    val challenges by challengesViewModel.myChallenges.collectAsStateWithLifecycle()
                    val publicChallenges by challengesViewModel.publicChallenges.collectAsStateWithLifecycle()
                    val recentNudges by viewModel.recentNudges.collectAsStateWithLifecycle()
                    val habits by challengesViewModel.myHabits.collectAsStateWithLifecycle()
                    val context = LocalContext.current
                    var runTour by remember { mutableStateOf(!com.example.utils.Prefs.isScreenTourSeen(context, "community")) }
                    LaunchedEffect(Unit) {
                        challengesViewModel.toast.collect { msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() }
                    }
                    LaunchedEffect(Unit) {
                        viewModel.toast.collect { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                    }
                    FriendsScreen(
                        modifier = Modifier.navigationBarsPadding(),
                        feed = feed,
                        requests = requests,
                        nudgeCount = nudges.size,
                        search = search,
                        suggestions = suggestions,
                        challenges = challenges,
                        publicChallenges = publicChallenges,
                        recentNudges = recentNudges,
                        habits = habits.map { HabitPick(it.id, it.title) },
                        myUid = challengesViewModel.myUid,
                        onSearch = viewModel::search,
                        onClearSearch = viewModel::clearSearch,
                        onLoadSuggestions = viewModel::loadSuggestions,
                        onSendRequest = viewModel::sendRequest,
                        onAccept = viewModel::accept,
                        onDecline = viewModel::decline,
                        onNudge = viewModel::nudge,
                        onRemoveFriend = viewModel::removeFriend,
                        onDismissNudges = viewModel::markNudgesSeen,
                        onInvite = ::shareInvite,
                        onCreateChallenge = challengesViewModel::create,
                        onLeaveChallenge = challengesViewModel::leave,
                        onAcceptChallenge = challengesViewModel::accept,
                        onDeclineChallenge = challengesViewModel::decline,
                        onJoinChallenge = challengesViewModel::join,
                        onMarkChallengeDone = challengesViewModel::markTodayDone,
                        runTour = runTour,
                        onTourFinished = { com.example.utils.Prefs.setScreenTourSeen(context, "community", true); runTour = false },
                        onSubScreenChanged = { sub -> (activity as? CommunityNavHost)?.onCommunitySubScreen(sub) },
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Don't leave the bar hidden if we navigate away while on a sub-screen.
        (activity as? CommunityNavHost)?.onCommunitySubScreen(false)
    }

    private fun shareInvite() {
        val text = "Build better habits with me on Apogee — let's keep each other accountable. https://play.google.com/store/apps/details?id=${requireContext().packageName}"
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(send, "Invite a friend"))
    }
}
