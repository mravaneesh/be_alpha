package com.example.create_ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.create_ui.databinding.ItemChallengeBinding
import com.example.create_ui.model.Challenge

class ChallengeAdapter(
    private val challenges: List<Challenge>,
    private val onClick: (Challenge) -> Unit
) : RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder>() {

    inner class ChallengeViewHolder(val binding: ItemChallengeBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val binding = ItemChallengeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChallengeViewHolder(binding)
    }

    override fun getItemCount() = challenges.size

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = challenges[position]
        with(holder.binding) {
            tvTitle.text = challenge.title
            Glide.with(ivBanner.context).load(challenge.bannerUrl).into(ivBanner)
            root.setOnClickListener { onClick(challenge) }
        }
    }
}
