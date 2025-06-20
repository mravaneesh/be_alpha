package com.example.goal_ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.goal_domain.model.Goal
import com.example.goal_ui.databinding.HabitItemGoalBinding
import com.example.ui.BubbleItemType
import com.example.ui.BubblePopup
import com.example.utils.CommonFun.applyScaleAnimation

class HabitGoalAdapter(
    private val onEditClick: (Goal) -> Unit,
    private val openAnalytics: (Goal) -> Unit,
    private val onDeleteClick: (Goal) -> Unit
) : ListAdapter<Goal, HabitGoalAdapter.GoalViewHolder>(GoalDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val binding = HabitItemGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GoalViewHolder(binding)
    }
    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = getItem(position)
        holder.bind(goal)
    }
    inner class GoalViewHolder(private val binding: HabitItemGoalBinding):
        RecyclerView.ViewHolder(binding.root) {

        private val bubblePopup = BubblePopup()
        init {
            binding.root.applyScaleAnimation()
            binding.root.setOnLongClickListener {
                val goal = getItem(adapterPosition)
                showBubblePopup(it, goal)
                true
            }
            binding.root.setOnClickListener {
                val goal = getItem(adapterPosition)
                Log.i("GoalAdapter", "Clicked on goal: $goal")
                openAnalytics(goal)
            }
        }

        fun bind(goal: Goal) {
            binding.tvGoalName.text = goal.title
        }

        private fun showBubblePopup(anchorView: View, goal: Goal) {
            val options = listOf(BubbleItemType.EDIT, BubbleItemType.DELETE)
            bubblePopup.setOnClickListener { _, itemType ->
                when (itemType) {
                    BubbleItemType.EDIT -> {
                        onEditClick(goal)
                    }
                    BubbleItemType.DELETE -> {
                        onDeleteClick(goal)
                    }
                }
            }
            bubblePopup.show(anchorView, options)
        }
    }

    class GoalDiffCallback : DiffUtil.ItemCallback<Goal>() {
        override fun areItemsTheSame(oldItem: Goal, newItem: Goal): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Goal, newItem: Goal): Boolean = oldItem == newItem
    }

}
