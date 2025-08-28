package com.example.profile_ui.view.pagerFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.profile_ui.adapter.PostsAdapter
import com.example.profile_ui.databinding.FragmentPostsBinding
import com.example.profile_ui.viewmodel.ProfileViewModel
import com.example.utils.CommonFun
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PostsFragment : Fragment() {

    private lateinit var binding: FragmentPostsBinding
    private val viewModel: ProfileViewModel by activityViewModels()
    private lateinit var adapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadUserPosts(CommonFun.getCurrentUserId()!!)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = PostsAdapter{ postClick ->
            //Handle Click
        }

        binding.postsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.postsRecyclerView.adapter = adapter

        observePosts()
    }
    private fun observePosts() {
        lifecycleScope.launch {
            viewModel.posts.collect { posts ->
                if (posts.isEmpty()) {
                    Log.i("PostsFragment", "No posts to show.")
                    binding.postsRecyclerView.visibility = View.GONE
                    binding.noPostsLayout.visibility = View.VISIBLE
                } else {
                    Log.i("PostsFragment", "Posts loaded: $posts")
                    binding.postsRecyclerView.visibility = View.VISIBLE
                    binding.noPostsLayout.visibility = View.GONE
                    adapter.submitList(posts)
                }
            }
        }
    }
}