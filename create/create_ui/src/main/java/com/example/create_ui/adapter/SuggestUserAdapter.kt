package com.example.create_ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.create_ui.databinding.ItemSuggestedUserBinding
import com.example.create_ui.model.SuggestedUser

class SuggestUserAdapter(
    private val context: Context,
    private val onFollowClick: (SuggestedUser) -> Unit
): RecyclerView.Adapter<SuggestUserAdapter.UserViewHolder>() {

    private val users = mutableListOf<SuggestedUser>()

    inner class UserViewHolder(val binding: ItemSuggestedUserBinding):
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSuggestedUserBinding.inflate(inflater, parent, false)
        return UserViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.binding.apply {
            tvUsername.text = user.username
            tvName.text = user.name
            btnFollow.text = if (user.isFollowing) "Following" else "Follow"
            btnFollow.setBackgroundResource(if (user.isFollowing) com.example.utils.R.drawable.button_secondary_bg else com.example.utils.R.drawable.button_primary_bg)
            btnFollow.setTextColor(if (!user.isFollowing) context.getColor(com.example.ui.R.color.white) else context.getColor(com.example.ui.R.color.black))
            btnFollow.setOnClickListener { onFollowClick(user) }
        }
    }
    fun submitList(newUsers: List<SuggestedUser>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }
}