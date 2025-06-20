package com.example.goal_ui.analytics.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.goal_domain.model.Goal
import com.example.goal_ui.analytics.model.DayStatus
import com.example.goal_ui.analytics.model.MonthItem
import com.example.goal_ui.databinding.ItemMonthBinding
import com.example.goal_ui.viewmodel.GoalViewModel
import com.example.utils.CommonFun
import java.time.YearMonth

class MonthAdapter(
    private var months: List<MonthItem>,
    private val goalViewModel: GoalViewModel,
    private var goal: Goal,
    private val fragmentManager: FragmentManager,
    private val onMonthSelected: (YearMonth) -> Unit
) :
    RecyclerView.Adapter<MonthAdapter.MonthViewHolder>() {

    inner class MonthViewHolder(val binding: ItemMonthBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val binding = ItemMonthBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MonthViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        holder.binding.daysRecyclerView.layoutManager = GridLayoutManager(holder.itemView.context, 7)
        holder.binding.daysRecyclerView.adapter = DayAdapter(months[position].days,fragmentManager){goalViewModel.updateGoalAnalytics(
            CommonFun.getCurrentUserId()!!,
            goal
        )}
    }

    override fun getItemCount(): Int = months.size

    fun updateMonths(newMonths: List<MonthItem>) {
        months = newMonths
        notifyDataSetChanged()
    }
    fun updateGoal(newGoal: Goal){
        this.goal = newGoal
        notifyDataSetChanged()
    }
}

