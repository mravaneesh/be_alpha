package com.example.create_ui.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.create_ui.R
import com.example.create_ui.adapter.ChallengeAdapter
import com.example.create_ui.adapter.SuggestUserAdapter
import com.example.create_ui.databinding.FragmentExploreBinding
import com.example.create_ui.model.Challenge
import com.example.create_ui.model.SuggestedUser
import com.example.create_ui.utils.ExploreCommonFun.getSuggestedChallenges
import com.example.create_ui.utils.ExploreCommonFun.toSuggestedUser
import com.example.utils.CommonFun
import com.example.utils.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private lateinit var suggestUserAdapter: SuggestUserAdapter
    private lateinit var challenges: List<Challenge>
    private val users = mutableListOf<SuggestedUser>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        challenges = getSuggestedChallenges()
        setupRecyclerView()
        fetchSuggestedUsers()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        sortAndSubmitUserList()
    }

    private fun sortAndSubmitUserList() {
        val sorted = users.sortedBy { it.isFollowing }
        suggestUserAdapter.submitList(sorted)
    }

    private fun fetchSuggestedUsers() {
        binding.rvPeopleToFollow.visibility = View.GONE
        binding.lottieProgress.visibility = View.VISIBLE
        val currentUserId = CommonFun.getCurrentUserId() ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(currentUserId).get()
            .addOnSuccessListener { snapshot ->
                val currentUser = snapshot.toObject(User::class.java)
                val currentUserFollowing = currentUser?.following ?: emptyList()

                db.collection("users").get()
                    .addOnSuccessListener { result ->
                        users.clear()
                        for (doc in result.documents) {
                            val user = doc.toObject(User::class.java)
                            if (user != null && user.id != currentUserId) {
                                val suggested = user.toSuggestedUser(currentUserFollowing)
                                users.add(suggested)
                            }
                        }
                        sortAndSubmitUserList()
                        binding.rvPeopleToFollow.visibility = View.VISIBLE
                        binding.lottieProgress.visibility = View.GONE
                    }
            }
    }

    private fun handleFollowAction(user: SuggestedUser) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = CommonFun.getCurrentUserId() ?: return

        val currentUserRef = db.collection("users").document(currentUserId)
        val targetUserRef = db.collection("users").document(user.id)

        val followingField = "following"
        val followersField = "followers"

        if (user.isFollowing) {
            // Unfollow
            currentUserRef.update(followingField, FieldValue.arrayRemove(user.id))
            targetUserRef.update(followersField, FieldValue.arrayRemove(currentUserId))
        } else {
            // Follow
            currentUserRef.update(followingField, FieldValue.arrayUnion(user.id))
            targetUserRef.update(followersField, FieldValue.arrayUnion(currentUserId))
        }

        val updatedUser = user.copy(isFollowing = !user.isFollowing)
        val index = users.indexOfFirst { it.id == user.id }
        if (index != -1) {
            users[index] = updatedUser
            sortAndSubmitUserList()
        }
    }

    private fun setupRecyclerView() {
        suggestUserAdapter = SuggestUserAdapter(requireContext()) { user ->
            handleFollowAction(user)
        }
        binding.rvPeopleToFollow.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = suggestUserAdapter 
        }

        for((index, challenge) in challenges.withIndex()){
            when(index){
                0 -> setupChallengeSection(challenge, binding.challenge75hard)
                1 -> setupChallengeSection(challenge, binding.challengeMeditation)
                2 -> setupChallengeSection(challenge, binding.challengeEatHealthy)
                3 -> setupChallengeSection(challenge, binding.challengeGym)
                4 -> setupChallengeSection(challenge, binding.challengeMarathon)
            }
        }
    }

    private fun setupChallengeSection(challenge: Challenge, cardView:View) {
        val title = cardView.findViewById<TextView>(R.id.tvTitle)
        val image = cardView.findViewById<ImageView>(R.id.ivBanner)

        title.text = challenge.title

        Glide.with(cardView.context)
            .load(challenge.bannerUrl)
            .centerCrop()
            .into(image)
    }
}