package com.example.goal_ui.adapter


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.goal_domain.model.Goal
import com.example.goal_ui.R
import com.example.goal_ui.databinding.HabitItemGoalBinding
import com.example.ui.BubbleItemType
import com.example.ui.BubblePopup
import com.example.ui.DialogConfirmation
import com.example.utils.CommonFun.animateOnClick
import java.time.LocalDate

class HabitGoalAdapter(
    private val onEditClick: (Goal) -> Unit,
    private val openAnalytics: (String) -> Unit,
    private val onStatusChange: (Goal) -> Unit,
    private val onDeleteClick: (Goal) -> Unit,
    private val fragmentManager: FragmentManager
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

        fun bind(goal: Goal) {
            binding.apply {

                tvGoalName.text = goal.title

                ivAnalytics.setOnClickListener {
                    animateOnClick(ivAnalytics)
                    Log.i("GoalAdapter", "Clicked on root: $goal")
                    openAnalytics(goal.id)
                }
                val today = LocalDate.now().toString()
                val status = goal.progress[today] ?: 3

                when (status) {
                    0 -> {
                        habitCompleteCircle.setBackgroundResource(R.drawable.bg_completed_day)
                        checkIcon.visibility = View.VISIBLE
                        habitCompleteCircle.isClickable = false
                    }
                    3 -> {
                        habitCompleteCircle.setBackgroundResource(com.example.utils.R.drawable.bg_circular_outline)
                        checkIcon.visibility = View.GONE
                        habitCompleteCircle.isClickable = true
                    }
                    2 -> {
                        completeLayout.visibility = View.GONE
                    }
                }

                tvDays.text = getSelectedDaysText(goal.selectedDays)

                root.setOnLongClickListener {
                    animateOnClick(root)
                    showBubblePopup(it, goal)
                    true
                }
                habitCompleteCircle.setOnClickListener{
                    Log.i("GoalAdapter", "Clicked on root: $goal")
                    if(checkIcon.isVisible) return@setOnClickListener
                    DialogConfirmation(
                        message = "Mark this habit as completed for the day?",
                        positiveText = "Yes",
                        negativeText = "Cancel",
                        onPositiveClick = {
                            onStatusChange(goal)
                            animateOnClick(habitCompleteCircle)

                            habitCompleteCircle.setBackgroundResource(R.drawable.bg_completed_day)
                            checkIcon.visibility = View.VISIBLE
                            habitCompleteCircle.isClickable = false
                        },
                        onNegativeClick = {
                            // Handle negative button click if needed
                        }
                    ).show(fragmentManager, "ConfirmComplete")
                }
            }
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

    private fun getSelectedDaysText(days: List<Int>): String {
        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        return days.sorted().joinToString("  |  ") { dayNames[it % 7] }
    }
}
