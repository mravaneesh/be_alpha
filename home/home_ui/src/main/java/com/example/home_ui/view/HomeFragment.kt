package com.example.home_ui.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.doOnLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.home_ui.R
import com.example.home_ui.adapter.FeedAdapter
import com.example.home_ui.databinding.FragmentHomeBinding
import com.example.utils.CommonFun
import com.example.utils.CommonFun.getUser
import com.example.utils.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var feedAdapter: FeedAdapter
    private val posts = mutableListOf<Post>()
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

        (binding.customProgress.parent as View).doOnLayout {
            updateProgress(6, 10)
        }
        binding.ivClose.setOnClickListener {
            binding.todaysHabitContainer.visibility = View.GONE
        }
        binding.ivAddPost.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_createPostFragment)
        }
        binding.ivNotification.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_notificationFragment)
        }
    }

    private fun setupRecyclerView() {
        feedAdapter = FeedAdapter(
            posts,
            onLikeClicked = { post ->
                // Handle like click
            },
            onCommentClicked = { post ->
                // Handle comment click
            }
        )
        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.adapter = feedAdapter
    }

    private fun loadFeed() {
        val userId = CommonFun.getCurrentUserId() ?: return
        FirebaseFirestore.getInstance()
            .collection("posts")
            .document(userId)
            .collection("userPosts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val postList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Post::class.java)
                }
                posts.addAll(postList)
                feedAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load posts", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProgress(completed: Int, total: Int) {
        val percent = if (total == 0) 0 else (completed * 100 / total)
        val layoutParams = binding.customProgress.layoutParams
        layoutParams.width = (binding.customProgress.parent as View).width * percent / 100
        binding.customProgress.layoutParams = layoutParams

        binding.tvProgressPercent.text = "$percent% done"
    }

}