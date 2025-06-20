package com.example.goal_ui.analytics.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.goal_ui.R
import com.example.goal_ui.analytics.model.CalendarDay
import com.example.goal_ui.analytics.model.DayStatus
import com.example.goal_ui.analytics.model.DayType
import com.example.goal_ui.databinding.ItemDayBinding
import com.example.ui.DialogConfirmation
import java.time.LocalDate

class DayAdapter(
    private val days: List<CalendarDay>,
    private val fragmentManager: FragmentManager,
    private val onDayStatusChanged: (CalendarDay) -> Unit
) :
    RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

    inner class DayViewHolder(val binding: ItemDayBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        val today = LocalDate.now()
        with(holder.binding) {
            dayText.text = day.date.dayOfMonth.toString()
            Log.i("DayAdapter", "onBindViewHolder: ${day.date} -> ${day.status}")
            when(day.status) {
                DayStatus.COMPLETED -> {
                    dayText.setTextColor(Color.WHITE)
                    dayText.setBackgroundResource(R.drawable.bg_completed_day)
                }

                DayStatus.MISSED -> {
                    dayText.setTextColor(Color.WHITE)
                    dayText.setBackgroundResource(R.drawable.bg_pending_day)
                }

                DayStatus.PENDING -> {
                    dayText.setTextColor(Color.BLACK)
                    dayText.setBackgroundResource(R.drawable.bg_current_day)
                }

                DayStatus.OUT_OF_RANGE -> {
                    dayText.setTextColor(Color.BLACK)
                    dayText.setBackgroundResource(android.R.color.transparent)
                }
            }

            when (day.dayType) {
                DayType.PREVIOUS, DayType.NEXT -> {
                    dayText.setTextColor(Color.GRAY)
                    dayText.setBackgroundResource(android.R.color.transparent)
                    root.isEnabled = false
                }

                DayType.CURRENT -> {
                    root.setOnClickListener {
                        if (day.date == today && day.status == DayStatus.PENDING) {
                            // Show confirmation dialog
                            DialogConfirmation(
                                message = "Mark this day as completed?",
                                positiveText = "Yes",
                                negativeText = "Cancel",
                                onPositiveClick = {
                                    day.status = DayStatus.COMPLETED
                                    onDayStatusChanged(day)
                                    notifyItemChanged(position)
                                },
                                onNegativeClick = {
                                    // Handle negative button click if needed
                                }
                            ).show(fragmentManager, "ConfirmComplete")
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = days.size
}


