package com.example.home_ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.home_ui.databinding.ItemPostBinding
import com.example.utils.model.Post

class FeedAdapter(
    private val posts: List<Post>,
    private val onLikeClicked: (Post) -> Unit,
    private val onCommentClicked: (Post) -> Unit) :
    RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    inner class FeedViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(binding.rvImages)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedViewHolder(binding)
    }

    override fun getItemCount(): Int = posts.size

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val post = posts[position]
        val binding = holder.binding

        binding.tvHabitTitle.text = post.habitTitle
        binding.tvCaption.text = post.caption
        binding.tvLikes.text = post.likes.toString()

        val imageAdapter = FeedImageAdapter(post.imageUrls)
        binding.rvImages.adapter = imageAdapter

        val layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvImages.layoutManager = layoutManager
//        binding.dotsIndicator.(binding.rvImages)

        binding.ivLike.setOnClickListener {
            onLikeClicked(post)
        }

        binding.ivComment.setOnClickListener {
            onCommentClicked(post)
        }
    }
}
