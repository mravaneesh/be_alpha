package com.example.notification_ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notification_domain.model.Notification
import com.example.notification_ui.databinding.ItemNotificationBinding

class NotificationAdapter :
    ListAdapter<Notification, NotificationAdapter.NotificationViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(
        private val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: Notification) {
            binding.title.text = notification.title
            binding.description.text = notification.description
        }
    }

    companion object Diff : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(old: Notification, new: Notification) =
            old.id == new.id

        override fun areContentsTheSame(old: Notification, new: Notification) =
            old == new
    }
}
