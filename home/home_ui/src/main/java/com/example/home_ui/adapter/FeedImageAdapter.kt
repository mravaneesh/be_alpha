package com.example.home_ui.adapter

import android.text.Layout
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.home_ui.databinding.ItemImagePagerBinding

class FeedImageAdapter(private val urls: List<String>) :
    RecyclerView.Adapter<FeedImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: ItemImagePagerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemImagePagerBinding.inflate(inflater, parent, false)
        return ImageViewHolder(binding)
    }

    override fun getItemCount(): Int = urls.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Glide.with(holder.binding.ivPost.context)
            .load(urls[position])
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.binding.ivPost)
    }
}
