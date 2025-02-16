package com.example.goal_ui.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.goal_domain.model.Goal
import com.example.goal_ui.R
import com.example.goal_ui.databinding.HabitItemGoalBinding

class HabitGoalAdapter : ListAdapter<Goal,
        HabitGoalAdapter.GoalViewHolder>(GoalDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val binding = HabitItemGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GoalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = getItem(position)
        holder.bind(goal)
    }

    inner class GoalViewHolder(private val binding: HabitItemGoalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(goal: Goal) {
            binding.tvGoalName.text = goal.title
            val typeface =  ResourcesCompat.getFont(itemView.context, R.font.raleway_semi_bold)
            binding.tvGoalName.typeface = typeface
            val drawable = ContextCompat.getDrawable(itemView.context, R.drawable.bg_general) as GradientDrawable
            drawable.setColor(goal.color)  // Set dynamic color
            binding.root.background = drawable
        }
    }


    class GoalDiffCallback : DiffUtil.ItemCallback<Goal>() {
        override fun areItemsTheSame(oldItem: Goal, newItem: Goal): Boolean {
            return oldItem.id == newItem.id  // Assuming Goal has a unique 'id' field
        }

        override fun areContentsTheSame(oldItem: Goal, newItem: Goal): Boolean {
            return oldItem == newItem  // Compare full goal object if required
        }
    }
}