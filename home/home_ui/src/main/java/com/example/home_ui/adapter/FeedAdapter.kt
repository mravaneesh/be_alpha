package com.example.home_ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.home_ui.databinding.ItemPostBinding
import com.example.home_domain.model.Post
import com.example.home_ui.R
import com.example.utils.CommonFun
import com.google.android.gms.common.internal.service.Common

class FeedAdapter(
    private val onLikeClicked: (Post) -> Unit,
    private val onCommentClicked: (Post) -> Unit
) : ListAdapter<Post, FeedAdapter.FeedViewHolder>(PostDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FeedViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val snapHelper = PagerSnapHelper()

        fun bind(post: Post) {
            binding.tvHabitTitle.text = post.habitTitle
            binding.tvCaption.text = post.caption
            binding.tvUsername.text = post.userName
            binding.tvUsernameCaption.text = post.userName
            binding.tvLikes.text = "${post.likes.size}"

            val imageAdapter = FeedImageAdapter(post.imageUrls)
            binding.rvImages.adapter = imageAdapter

            // LayoutManager & SnapHelper setup
            if (binding.rvImages.layoutManager == null) {
                binding.rvImages.layoutManager = LinearLayoutManager(
                    binding.root.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                snapHelper.attachToRecyclerView(binding.rvImages)
//                binding.dotsIndicator.attachTo(binding.rvImages)
            }

            val isLiked = post.likes.contains(CommonFun.getCurrentUserId()!!)
            binding.ivLike.setImageResource(
                if (!isLiked) R.drawable.ic_like else R.drawable.ic_like_filled
            )

            binding.ivLike.setOnClickListener {
                CommonFun.animateOnClick(binding.ivLike)
                onLikeClicked(post)
            }

            binding.ivComment.setOnClickListener {
                onCommentClicked(post)
            }
        }
    }

    fun updatePost(updatedPost: Post) {
        val currentList = currentList.toMutableList()
        val index = currentList.indexOfFirst { it.id == updatedPost.id }
        if (index != -1) {
            currentList[index] = updatedPost
            submitList(currentList)
        }
    }


    companion object {
        val PostDiffCallback = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem == newItem
            }
        }
    }
}
