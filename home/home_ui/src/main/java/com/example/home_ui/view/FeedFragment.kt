package com.example.home_ui.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.ai_domain.model.AiSuggestion
import com.example.designsystem.theme.PactTheme
import com.example.home_domain.model.Post
import com.example.home_ui.compose.FeedScreen
import com.example.home_ui.viewModel.HomeViewModel
import com.example.utils.CommonFun
import com.example.utils.Resource
import com.example.utils.model.GoalModel
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

class FeedFragment : Fragment() {
    companion object {
        private const val TAG = "FeedFragment"
    }

    private val viewModel: HomeViewModel by activityViewModels()

    // UI state surfaced to Compose; all mutations happen from the existing logic below.
    private var posts by mutableStateOf<List<Post>>(emptyList())
    private var isLoading by mutableStateOf(true)
    private var suggestion by mutableStateOf<AiSuggestion?>(null)
    private var showHabitCard by mutableStateOf(false)
    private var habitPercent by mutableIntStateOf(0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PactTheme {
                    FeedScreen(
                        posts = posts,
                        isLoading = isLoading,
                        currentUserId = CommonFun.getCurrentUserId().orEmpty(),
                        onLike = ::toggleLike,
                        onComment = { /* comments not yet implemented */ },
                        suggestion = suggestion,
                        showHabitCard = showHabitCard,
                        habitPercent = habitPercent,
                        onCloseHabitCard = {
                            showHabitCard = false
                            viewModel.isHabitCardClosed = true
                        },
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showHabitCard = !viewModel.isHabitCardClosed
        if (showHabitCard) loadTodaysHabitProgress()
        loadFeed()
        viewModel.loadSuggestions(userId = CommonFun.getCurrentUserId()!!)
        observeViewModel()
    }

    private fun toggleLike(post: Post) {
        val currentUserId = CommonFun.getCurrentUserId() ?: return
        val isLiked = post.likes.contains(currentUserId)

        val updatedLikes = if (isLiked) post.likes - currentUserId else post.likes + currentUserId
        val updatedPost = post.copy(likes = updatedLikes)

        val postRef = FirebaseFirestore.getInstance()
            .collection("posts")
            .document(post.userId)
            .collection("userPosts")
            .document(post.id)

        // Optimistically update the UI, then reconcile with the server result.
        updatePostInState(updatedPost)

        postRef.update("likes", updatedLikes)
            .addOnFailureListener { e ->
                updatePostInState(post) // revert
                if (isAdded) {
                    Toast.makeText(requireContext(), "Couldn't update like", Toast.LENGTH_SHORT).show()
                }
                Log.e(TAG, "Failed to toggle like for post ${post.id}", e)
            }
    }

    private fun updatePostInState(updated: Post) {
        posts = posts.map { if (it.id == updated.id) updated else it }
    }

    private fun loadFeed() {
        viewModel.loadFeed(userId = CommonFun.getCurrentUserId()!!)
    }

    private fun observeViewModel() {
        viewModel.feedState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    posts = result.data.orEmpty()
                    isLoading = false
                }
                is Resource.Error -> {
                    isLoading = false
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> isLoading = true
            }
        }

        viewModel.suggestions.observe(viewLifecycleOwner) { result ->
            Log.i(TAG, "AI suggestion received: $result")
            suggestion = result
        }
    }

    private fun loadTodaysHabitProgress() {
        val userId = CommonFun.getCurrentUserId() ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("goals")
            .document(userId)
            .collection("Habit")
            .get()
            .addOnSuccessListener { snapshot ->
                val today = LocalDate.now()
                val todayIndex = today.dayOfWeek.value % 7 // Sunday = 0

                var completedCount = 0
                var totalScheduledToday = 0

                for (doc in snapshot.documents) {
                    val goal = doc.toObject(GoalModel::class.java) ?: continue
                    val progress = goal.progress
                    val selectedDays = goal.selectedDays

                    if (selectedDays.contains(todayIndex)) {
                        totalScheduledToday++
                        if (progress[today.toString()] == 0) completedCount++
                    }
                }

                habitPercent = if (totalScheduledToday == 0) 0 else completedCount * 100 / totalScheduledToday
            }
            .addOnFailureListener {
                Log.e("ProgressLoad", "Failed to load habits: ${it.message}")
            }
    }
}
