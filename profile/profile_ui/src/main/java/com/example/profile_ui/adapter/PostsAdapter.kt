package com.example.profile_ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.home_domain.model.Post
import com.example.profile_ui.databinding.ItemPostGridBinding

class PostsAdapter(
    private var posts: List<Post> = emptyList(),
    private val onPostClick: (Post) -> Unit
) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    inner class PostViewHolder(private val binding: ItemPostGridBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            // Show only the first image
            val firstImageUrl = post.imageUrls.firstOrNull()
            Glide.with(binding.postImageView.context)
                .load(firstImageUrl)
                .centerCrop()
                .into(binding.postImageView)

            binding.root.setOnClickListener {
                onPostClick(post)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostGridBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PostViewHolder(binding)
    }

    override fun getItemCount() = posts.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    fun submitList(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}
