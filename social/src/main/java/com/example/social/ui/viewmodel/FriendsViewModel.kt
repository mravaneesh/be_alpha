package com.example.social.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.social.domain.model.FriendRequest
import com.example.social.domain.model.FriendState
import com.example.social.domain.model.FriendSummary
import com.example.social.domain.model.LeaderboardEntry
import com.example.social.domain.model.Nudge
import com.example.social.domain.model.UserSearchResult
import com.example.social.domain.repository.SocialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchState(
    val loading: Boolean = false,
    val results: List<UserSearchResult> = emptyList(),
    val searched: Boolean = false,
)

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val repo: SocialRepository,
) : ViewModel() {

    val feed: StateFlow<List<FriendSummary>> =
        repo.observeFeed().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leaderboard: StateFlow<List<LeaderboardEntry>> =
        repo.observeLeaderboard().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val requests: StateFlow<List<FriendRequest>> =
        repo.observeIncomingRequests().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nudges: StateFlow<List<Nudge>> =
        repo.observeUnseenNudges().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentNudges: StateFlow<List<Nudge>> =
        repo.observeRecentNudges().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _search = MutableStateFlow(SearchState())
    val search: StateFlow<SearchState> = _search

    /** Suggested people to add, shown before the user types. Loaded once on demand. */
    private val _suggestions = MutableStateFlow<List<UserSearchResult>>(emptyList())
    val suggestions: StateFlow<List<UserSearchResult>> = _suggestions

    private val _toast = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toast: SharedFlow<String> = _toast.asSharedFlow()

    fun search(query: String) {
        if (query.isBlank()) { _search.value = SearchState(); return }
        viewModelScope.launch {
            _search.value = SearchState(loading = true)
            val results = runCatching { repo.searchUsers(query) }.getOrDefault(emptyList())
            _search.value = SearchState(results = results, searched = true)
        }
    }

    fun clearSearch() { _search.value = SearchState() }

    fun loadSuggestions() {
        if (_suggestions.value.isNotEmpty()) return
        viewModelScope.launch {
            _suggestions.value = runCatching { repo.suggestUsers() }.getOrDefault(emptyList())
        }
    }

    fun sendRequest(uid: String) {
        viewModelScope.launch {
            runCatching { repo.sendFriendRequest(uid) }
            fun markPending(list: List<UserSearchResult>) = list.map {
                if (it.summary.uid == uid) it.copy(state = FriendState.PENDING) else it
            }
            _search.value = _search.value.copy(results = markPending(_search.value.results))
            _suggestions.value = markPending(_suggestions.value)
        }
    }

    fun accept(request: FriendRequest) { viewModelScope.launch { runCatching { repo.acceptRequest(request) } } }
    fun decline(request: FriendRequest) { viewModelScope.launch { runCatching { repo.declineRequest(request) } } }

    fun nudge(uid: String) {
        viewModelScope.launch {
            runCatching { repo.sendNudge(uid) }
                .onSuccess { _toast.tryEmit("Cheer sent 👏") }
                .onFailure { _toast.tryEmit("Couldn't send cheer: ${it.message ?: "unknown error"}") }
        }
    }
    fun markNudgesSeen() { viewModelScope.launch { runCatching { repo.markNudgesSeen() } } }
    fun removeFriend(uid: String) { viewModelScope.launch { runCatching { repo.removeFriend(uid) } } }
}
