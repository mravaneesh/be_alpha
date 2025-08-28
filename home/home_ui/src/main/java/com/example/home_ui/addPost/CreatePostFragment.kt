package com.example.home_ui.addPost

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.home_domain.model.Post
import com.example.home_ui.addPost.adapter.CreatePostAdapter
import com.example.home_ui.addPost.utils.ImagePreviewDialogFragment
import com.example.home_ui.databinding.FragmentCreatePostBinding
import com.example.utils.CommonFun
import com.example.utils.shared_viewmodel.CreatePostViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreatePostFragment : Fragment() {
    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!
    private var selectedHabitTitle: String? = null
    private lateinit var createPostAdapter: CreatePostAdapter
    private val postViewModel: CreatePostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        selectedHabitTitle = arguments?.getString("habitTitle")
        setupViews()
        observeViewModel()
        return binding.root
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            postViewModel.sectionPreviews.collectLatest { previews ->
                val bitmaps = previews.values.mapNotNull { it.bitmap }
                createPostAdapter.submitList(bitmaps)
            }
        }
    }


    private fun setupViews() {
        setupRecyclerView()
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.tvHabitTitle.text = selectedHabitTitle

        binding.btnPost.setOnClickListener {
            lifecycleScope.launch {
                val userId = CommonFun.getCurrentUserId() ?: return@launch
                val userName = CommonFun.getUser()?.username ?: return@launch
                val caption = binding.etCaption.text.toString()
                val bitmaps = postViewModel.getCapturedBitmaps()

                if (bitmaps.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Please select at least one section",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                try {
                    val urls = uploadAllBitmaps(requireContext(), bitmaps)
                    Log.d("S3Upload", "Uploaded URLs: $urls")
                    //                binding.postProgress.visibility = View.VISIBLE
                    val postId = FirebaseFirestore.getInstance()
                        .collection("posts").document(userId)
                        .collection("userPosts").document().id


                    val post = Post(
                        id = postId,
                        userId = userId,
                        userName = userName,
                        habitTitle = selectedHabitTitle?:"",
                        caption = caption,
                        imageUrls = urls,
                        createdAt = System.currentTimeMillis()
                    )
                    FirebaseFirestore.getInstance().collection("posts")
                        .document(userId)
                        .collection("userPosts")
                        .document(postId)
                        .set(post)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Post shared 🎉", Toast.LENGTH_SHORT)
                                .show()
                            postViewModel.clearPreviews()
                            binding.etCaption.text.clear()
                            findNavController().navigate(
                                com.example.home_ui.R.id.action_createPostFragment_to_homeFragment,
                                null,
                                NavOptions.Builder()
                                    .setPopUpTo(com.example.home_ui.R.id.homeFragment,true).build()
                            )
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to post", Toast.LENGTH_SHORT)
                                .show()
                        }
                        .addOnCompleteListener {
//                        binding.progressBar.visibility = View.GONE
                        }
                } catch (e: Exception) {
                    Log.e("S3Upload", "Error uploading to S3: ${e.message}")
                }
            }
        }
    }

    private fun setupRecyclerView() {
        createPostAdapter = CreatePostAdapter{bitmap ->
            ImagePreviewDialogFragment(bitmap).show(childFragmentManager, "ImagePreview")
        }
        binding.rvSelectedImages.apply {
            adapter = createPostAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private suspend fun uploadAllBitmaps(context: Context, bitmaps: List<Bitmap>): List<String> {
        val urls = mutableListOf<String>()
        for (bitmap in bitmaps) {
            val url = AddPostUtils.uploadToS3(context, bitmap)
            urls.add(url)
        }
        return urls
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}