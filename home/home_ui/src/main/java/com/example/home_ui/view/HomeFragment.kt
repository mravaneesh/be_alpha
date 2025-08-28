package com.example.home_ui.view

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.home_domain.model.Post
import com.example.home_ui.R
import com.example.home_ui.adapter.FeedAdapter
import com.example.home_ui.databinding.FragmentHomeBinding
import com.example.home_ui.viewModel.HomeViewModel
import com.example.utils.CommonFun
import com.example.utils.CommonFun.getUser
import com.example.utils.Resource
import com.example.utils.model.GoalModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var feedAdapter: FeedAdapter
    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupFragment()
        setupRecyclerView()
        loadFeed()
        return binding.root
    }

    private fun setupFragment() {
        lifecycleScope.launch {
            val user = getUser()
            Log.i("HomeFragment", "setupFragment: $user")
            user?.name?.let { fullName ->
                Log.i("HomeFragment", "setupFragment: $fullName")
                val firstName = fullName.trim().split(" ").firstOrNull() ?: "there"
                binding.tvWelcome.text = "Hi, $firstName 👋"
            }
        }
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val today = Calendar.getInstance().time
        val formattedDate = "Today, ${dateFormat.format(today)}"
        binding.tvDate.text = formattedDate

        if (!viewModel.isHabitCardClosed) {
            binding.todaysHabitContainer.visibility = View.VISIBLE
            (binding.customProgress.parent as View).doOnLayout {
                loadTodaysHabitProgress()
            }
        }

        binding.ivClose.setOnClickListener {
            binding.todaysHabitContainer.visibility = View.GONE
            viewModel.isHabitCardClosed = true
        }
        binding.ivAddPost.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_habitSelectFragment)
        }
        binding.ivNotification.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_notificationFragment)
        }
    }

    private fun setupRecyclerView() {
        feedAdapter = FeedAdapter(
            onLikeClicked = { post ->
                toggleLike(post)
            },
            onCommentClicked = { post ->
                // Handle comment click
            }
        )
        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.adapter = feedAdapter
    }

    private fun toggleLike(post: Post) {
        val currentUserId = CommonFun.getCurrentUserId()?: return
        val isLiked = post.likes.contains(currentUserId)

        val updatedLikes = if (isLiked) {
            post.likes - currentUserId
        } else {
            post.likes + currentUserId
        }

        val updatedPost = post.copy(likes = updatedLikes)

        val postRef = FirebaseFirestore.getInstance()
            .collection("posts")
            .document(post.userId)
            .collection("userPosts")
            .document(post.id)

        postRef.update("likes", updatedLikes)
            .addOnSuccessListener {
                feedAdapter.updatePost(updatedPost) // Update the UI
            }
    }


    private fun loadFeed() {
        viewModel.loadFeed(userId = CommonFun.getCurrentUserId()!!)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.feedState.observe(viewLifecycleOwner) { result ->
            when(result) {
                is Resource.Success -> {
                    feedAdapter.submitList(result.data)
                }
                is Resource.Error ->{
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    private fun updateProgress(completed: Int, total: Int) {
        val percent = if (total == 0) 0 else (completed * 100 / total)

        // Delay until layout is drawn
        (binding.customProgress.parent as View).doOnLayout {
            val fullWidth = it.width

            // Calculate target width for progress
            val targetWidth = (fullWidth * percent / 100f).toInt()

            // Animate from current width to target width
            val anim = ValueAnimator.ofInt(0, targetWidth)
            anim.duration = 800 // Duration in ms
            anim.addUpdateListener { animator ->
                val newWidth = animator.animatedValue as Int
                binding.customProgress.layoutParams.width = newWidth
                binding.customProgress.requestLayout()
            }
            anim.start()

            // Optional: update percentage text
            binding.tvProgressPercent.text = "$percent% completed"
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
                        val todayStr = today.toString()

                        if (progress[todayStr] == 0) {
                            completedCount++
                        }
                    }
                }

                updateProgress(completedCount, totalScheduledToday)
            }
            .addOnFailureListener {
                Log.e("ProgressLoad", "Failed to load habits: ${it.message}")
            }
    }


}