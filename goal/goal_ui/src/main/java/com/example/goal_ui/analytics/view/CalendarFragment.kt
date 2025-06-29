package com.example.goal_ui.analytics.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.goal_domain.model.Goal
import com.example.goal_ui.analytics.adapter.MonthAdapter
import com.example.goal_ui.analytics.model.CalendarDay
import com.example.goal_ui.analytics.model.DayStatus
import com.example.goal_ui.analytics.model.DayType
import com.example.goal_ui.analytics.model.MonthItem
import com.example.goal_ui.databinding.FragmentCalendarBinding
import com.example.goal_ui.viewmodel.GoalViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var monthAdapter: MonthAdapter
    private var monthList = mutableListOf<MonthItem>()
    private var currentMonth = YearMonth.now()
    private val goalViewModel: GoalViewModel by activityViewModels()
    private lateinit var goal: Goal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        goal = arguments?.getParcelable("goal")
            ?: throw IllegalArgumentException("Goal data is missing")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        binding.tvYearMonth.setOnClickListener {
            showMonthYearPickerDialog()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        monthList = generateFiveMonths(currentMonth).toMutableList()
        monthAdapter = MonthAdapter(
            monthList,
            goalViewModel,
            goal,
            parentFragmentManager
        ) { updatedMonth ->
            currentMonth = updatedMonth
        }

        setupRecyclerView()
        updateYearMonthText(currentMonth)
    }

    private fun observeGoalData() {
        lifecycleScope.launch {
            goalViewModel.habitGoals.collectLatest { state->
                    val selectedGoal = state.goals.find { it.id == goal.id }
                    if(selectedGoal != null){
                        goal = selectedGoal
                        if(!::monthAdapter.isInitialized){
                            monthList = generateFiveMonths(currentMonth).toMutableList()
                            monthAdapter = MonthAdapter(monthList, goalViewModel,goal,parentFragmentManager) { updatedMonth, ->
                                currentMonth = updatedMonth
                            }
                            setupRecyclerView()
                        }else{
                            monthAdapter.updateGoal(goal)
                        }
                        updateYearMonthText(currentMonth)
                    }
                    Log.d("CalendarFragment", "Goal data: ${goal.progress}")
                }
            }
    }

    private fun updateYearMonthText(month: YearMonth) {
        binding.tvYearMonth.text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        val slideUp = AnimationUtils.loadAnimation(requireContext(), com.example.utils.R.anim.slide_up)
        binding.tvYearMonth.startAnimation(slideUp)
    }

    private fun showMonthYearPickerDialog() {
        val currentYear = currentMonth.year
        val currentMonthValue = currentMonth.monthValue

        val dialog = MonthYearPickerDialog(requireContext(), currentYear, currentMonthValue) { year, month ->
            val targetMonth = YearMonth.of(year, month)
            scrollToMonth(targetMonth)
        }
        dialog.show()
    }

    private fun setupRecyclerView() {
        binding.monthRecyclerView.apply {
            adapter = monthAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            scrollToPosition(2)
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(this)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val currentPosition = layoutManager.findFirstVisibleItemPosition()

                        when (currentPosition) {
                            0, 1 -> {
                                currentMonth = currentMonth.minusMonths(1)
                                updateMonthList()
                            }
                            3, 4 -> {
                                currentMonth = currentMonth.plusMonths(1)
                                updateMonthList()
                            }
                        }
                        updateYearMonthText(currentMonth)
                    }
                }
            })
        }
    }

    private fun updateMonthList() {
        monthList = generateFiveMonths(currentMonth).toMutableList()
        monthAdapter.updateMonths(monthList)
        binding.monthRecyclerView.scrollToPosition(2)
        updateYearMonthText(currentMonth)
    }

    private fun generateFiveMonths(centerMonth: YearMonth): List<MonthItem> {
        return listOf(
            MonthItem(centerMonth.minusMonths(2), generateCalendarDays(centerMonth.minusMonths(2))),
            MonthItem(centerMonth.minusMonths(1), generateCalendarDays(centerMonth.minusMonths(1))),
            MonthItem(centerMonth, generateCalendarDays(centerMonth)),
            MonthItem(centerMonth.plusMonths(1), generateCalendarDays(centerMonth.plusMonths(1))),
            MonthItem(centerMonth.plusMonths(2), generateCalendarDays(centerMonth.plusMonths(2)))
        )
    }

    private fun generateCalendarDays(selectedMonth: YearMonth): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val today = LocalDate.now()

        val firstDayOfMonth = selectedMonth.atDay(1).dayOfWeek.value % 7
        val previousMonth = selectedMonth.minusMonths(1)
        val daysInPreviousMonth = previousMonth.lengthOfMonth()

        for (i in (daysInPreviousMonth - firstDayOfMonth + 1)..daysInPreviousMonth) {
            days.add(CalendarDay(previousMonth.atDay(i), DayType.PREVIOUS,getDayStatus(previousMonth.atDay(i))))
        }

        for (i in 1..selectedMonth.lengthOfMonth()) {
            val currentDate = selectedMonth.atDay(i)
            val isSelectable = currentDate == today
            days.add(CalendarDay(selectedMonth.atDay(i), DayType.CURRENT,getDayStatus(currentDate),isSelectable))
        }

        val remainingDays = 42 - days.size
        val nextMonth = selectedMonth.plusMonths(1)
        for (i in 1..remainingDays) {
            days.add(CalendarDay(nextMonth.atDay(i), DayType.NEXT,getDayStatus(nextMonth.atDay(i))))
        }
        return days
    }

    private fun getDayStatus(date: LocalDate?): DayStatus {
        Log.d("CalendarFragment", "Goal progress: $date -> ${goal.progress[date.toString()]}")
        return when (goal.progress[date.toString()]) {
            0 -> DayStatus.COMPLETED
            1 -> DayStatus.MISSED
            3 -> DayStatus.PENDING
            else -> DayStatus.OUT_OF_RANGE
        }
    }

    private fun scrollToMonth(targetMonth: YearMonth) {
        currentMonth = targetMonth
        updateMonthList()
    }

}


