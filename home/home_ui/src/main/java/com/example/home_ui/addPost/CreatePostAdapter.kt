package com.example.home_ui.addPost

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.home_ui.databinding.ItemPostPreviewBinding

class CreatePostAdapter(
    private val onItemClick: (Bitmap) -> Unit
): RecyclerView.Adapter<CreatePostAdapter.CreatePostViewHolder>() {

    private val bitmaps = mutableListOf<Bitmap>()

    fun submitList(newList: List<Bitmap>) {
        bitmaps.clear()
        bitmaps.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CreatePostAdapter.CreatePostViewHolder {
        val binding = ItemPostPreviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CreatePostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CreatePostAdapter.CreatePostViewHolder, position: Int) {
        holder.bind(bitmaps[position])
    }

    override fun getItemCount(): Int {
       return bitmaps.size
    }

    inner class CreatePostViewHolder(private val binding: ItemPostPreviewBinding):
            RecyclerView.ViewHolder(binding.root) {
        fun bind(bitmap: Bitmap) {
            binding.imagePreview.setImageBitmap(bitmap)
            binding.imagePreview.setOnClickListener {
                onItemClick(bitmap)
            }
        }
    }
}