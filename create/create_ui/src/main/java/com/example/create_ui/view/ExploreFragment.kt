package com.example.create_ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.example.create_ui.compose.ExploreScreen
import com.example.create_ui.model.Challenge
import com.example.create_ui.model.SuggestedUser
import com.example.create_ui.utils.ExploreCommonFun.getSuggestedChallenges
import com.example.create_ui.utils.ExploreCommonFun.toSuggestedUser
import com.example.designsystem.theme.PactTheme
import com.example.utils.CommonFun
import com.example.utils.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ExploreFragment : Fragment() {

    private val users = mutableStateListOf<SuggestedUser>()
    private var usersLoading by mutableStateOf(true)
    private lateinit var challenges: List<Challenge>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        challenges = getSuggestedChallenges()
        fetchSuggestedUsers()
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PactTheme {
                    ExploreScreen(
                        users = users.toList(),
                        usersLoading = usersLoading,
                        onFollow = ::handleFollowAction,
                        challenges = challenges,
                        onChallengeClick = { /* challenge details not yet implemented */ },
                    )
                }
            }
        }
    }

    private fun submitSorted(list: List<SuggestedUser>) {
        val sorted = list.sortedBy { it.isFollowing }
        users.clear()
        users.addAll(sorted)
    }

    private fun fetchSuggestedUsers() {
        usersLoading = true
        val currentUserId = CommonFun.getCurrentUserId() ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(currentUserId).get()
            .addOnSuccessListener { snapshot ->
                val currentUser = snapshot.toObject(User::class.java)
                val currentUserFollowing = currentUser?.following ?: emptyList()

                db.collection("users").get()
                    .addOnSuccessListener { result ->
                        val fetched = result.documents.mapNotNull { it.toObject(User::class.java) }
                            .filter { it.id != currentUserId }
                            .map { it.toSuggestedUser(currentUserFollowing) }
                        submitSorted(fetched)
                        usersLoading = false
                    }
                    .addOnFailureListener { usersLoading = false }
            }
            .addOnFailureListener { usersLoading = false }
    }

    private fun handleFollowAction(user: SuggestedUser) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = CommonFun.getCurrentUserId() ?: return

        val currentUserRef = db.collection("users").document(currentUserId)
        val targetUserRef = db.collection("users").document(user.id)

        if (user.isFollowing) {
            currentUserRef.update("following", FieldValue.arrayRemove(user.id))
            targetUserRef.update("followers", FieldValue.arrayRemove(currentUserId))
        } else {
            currentUserRef.update("following", FieldValue.arrayUnion(user.id))
            targetUserRef.update("followers", FieldValue.arrayUnion(currentUserId))
        }

        // Optimistic local update + re-sort, matching prior behavior.
        val updated = users.map { if (it.id == user.id) user.copy(isFollowing = !user.isFollowing) else it }
        submitSorted(updated)
    }
}
