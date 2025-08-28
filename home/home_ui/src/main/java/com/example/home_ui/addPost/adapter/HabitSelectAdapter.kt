package com.example.home_ui.addPost.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.home_ui.databinding.ItemHabitBinding
import com.example.utils.model.GoalModel

class HabitSelectAdapter(
    private val habits: List<GoalModel>,
    private val onHabitClick: (GoalModel) -> Unit
) : RecyclerView.Adapter<HabitSelectAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(val binding: ItemHabitBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        holder.binding.tvGoalName.text = habit.title
        if(habit.description.isNotEmpty()){
            holder.binding.tvDays.text = habit.description
            holder.binding.tvDays.visibility = View.VISIBLE
        }
        holder.binding.root.setOnClickListener { onHabitClick(habit) }
    }

    override fun getItemCount(): Int = habits.size
}
