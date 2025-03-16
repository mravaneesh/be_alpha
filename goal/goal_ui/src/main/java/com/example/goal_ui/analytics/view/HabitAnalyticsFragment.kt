package com.example.goal_ui.analytics.view

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.goal_domain.model.Goal
import com.example.goal_ui.R
import com.example.goal_ui.analytics.model.CalendarDay
import com.example.goal_ui.analytics.model.DayStatus
import com.example.goal_ui.analytics.model.DayType
import com.example.goal_ui.databinding.FragmentHabitAnalyticsBinding
import com.example.goal_ui.viewmodel.GoalViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

class HabitAnalyticsFragment : Fragment() {

    private var _binding: FragmentHabitAnalyticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GoalViewModel by activityViewModels()
    private lateinit var lineChart: LineChart
    private lateinit var selectedDays: List<DayOfWeek>
    private lateinit var habitData: List<CalendarDay>
    private lateinit var goal:Goal

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitAnalyticsBinding.inflate(inflater, container, false)

        goal = arguments?.getParcelable("goal")?:
        throw IllegalArgumentException("Goal argument is missing")
        setUpFragment()
        initCalendar()
        lineChart = binding.lineChart

        selectedDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        habitData = generateSampleHabitData()
        setupLineChart(true)
        displaySuccessRateChart()

        return binding.root
    }

    private fun setUpFragment() {
        binding.toolbar.title = goal.title
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        observeViewmodel()
    }

    private fun observeViewmodel() {
        lifecycleScope.launch{
            viewModel.habitGoals.collectLatest { state ->
                when {
                    state.isLoading -> {
                        Toast.makeText(requireContext(), "Loading Habits...", Toast.LENGTH_SHORT).show()
                    }
                    state.error.isNotBlank() -> {
                        Toast.makeText(requireContext(), "Error: ${state.error}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val selectedGoal = state.goals.find { it.id == goal.id }
                        selectedGoal?.let { filteredGoal ->
                            updateTextWithAnimation(binding.currentStreak, filteredGoal.currentStreak)
                            updateTextWithAnimation(binding.bestStreak, filteredGoal.bestStreak)
                            updateTextWithAnimation(binding.successRate, filteredGoal.successRate)
                            updateTextWithAnimation(binding.totalDays, filteredGoal.totalCompleted)
                        } ?: Log.e("GoalFilter", "Goal with ID ${goal.id} not found")
                    }
                }
            }
        }
    }

    private fun updateTextWithAnimation(textView: TextView, targetValue: Int) {
        val formattedValue = String.format(Locale.getDefault(), "%d", targetValue)

        if (textView.text.toString() == formattedValue) return // Skip animation if value hasn't changed
        textView.text = formattedValue
        val slideUp = AnimationUtils.loadAnimation(textView.context, com.example.utils.R.anim.slide_up)
        textView.startAnimation(slideUp)
    }

    private fun initCalendar() {
        val calendarFragment = CalendarFragment().apply {
            arguments = Bundle().apply {
                putParcelable("goal", goal)
            }
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.calendarContainer, calendarFragment)
            .commit()
    }

    private fun setupLineChart(isWeekly: Boolean) {
        val entries = mutableListOf<Entry>()
        val dateLabels = mutableListOf<String>()

        val today = LocalDate.now()

        if (isWeekly) {
            // Weekly View: Show selected days within the current week
            selectedDays.forEachIndexed { index, dayOfWeek ->
                val date = today.with(dayOfWeek)
                entries.add(Entry(index.toFloat(), (50..100).random().toFloat()))
                dateLabels.add(date.dayOfMonth.toString()) // Show day number for weekly
            }
        } else {
            // Monthly View: Show selected days within the current month
            val monthStart = today.withDayOfMonth(1)
            (0 until today.lengthOfMonth()).forEach { day ->
                val date = monthStart.plusDays(day.toLong())
                if (date.dayOfWeek in selectedDays) {
                    entries.add(Entry(entries.size.toFloat(), (50..100).random().toFloat()))
                    dateLabels.add(date.dayOfMonth.toString()) // Show day number for monthly
                }
            }
        }

        val dataSet = LineDataSet(entries, "Success Rate").apply {
            color = ContextCompat.getColor(requireContext(), R.color.button_primary)
            valueTextColor = Color.BLACK
            lineWidth = 3f
            setDrawCircles(true)
            setCircleColor(Color.BLACK)
            setCircleRadius(5f)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        with(binding.lineChart) {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false

            // Disable Y-axis grid lines
            axisLeft.setDrawGridLines(false)
            axisRight.setDrawGridLines(false)

            // X-Axis Configuration
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f  // Ensures each point corresponds to one entry
                setLabelCount(dateLabels.size, true)  // Ensures all labels are visible
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return dateLabels.getOrNull(value.toInt()) ?: ""
                    }
                }
            }

            // Y-Axis Configuration
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 100f
                setDrawGridLines(false)
                setDrawLabels(true)
            }
            axisRight.isEnabled = false

            setTouchEnabled(true)
            setPinchZoom(false)
            setScaleEnabled(false)

            animateX(1000)
            invalidate() 
        }
    }





    private fun displaySuccessRateChart() {
        val entries = mutableListOf<Entry>()

        val groupedData = habitData
            .groupBy { it.date.withDayOfMonth(1) }  // Group by month
            .toSortedMap()

        groupedData.entries.forEachIndexed { index, entry ->
            val successRate = calculateSuccessRate(selectedDays, entry.value).toFloat()
            entries.add(Entry(index.toFloat(), successRate))
        }

        val dataSet = LineDataSet(entries, "Success Rate").apply {
            color = ColorTemplate.MATERIAL_COLORS[0]
            valueTextColor = ColorTemplate.getHoloBlue()
            valueTextSize = 12f
            setCircleColor(ColorTemplate.MATERIAL_COLORS[0])
            setDrawFilled(true)
            fillColor = ColorTemplate.MATERIAL_COLORS[0]
            mode = LineDataSet.Mode.CUBIC_BEZIER  // Smooth curve effect
        }

        lineChart.data = LineData(dataSet)
        lineChart.invalidate() // Refresh the chart
    }

    private fun calculateSuccessRate(
        selectedDays: List<DayOfWeek>,
        habitData: List<CalendarDay>
    ): Double {
        val validDays = habitData.filter { it.date.dayOfWeek in selectedDays }
        val completedDays = validDays.count { it.status == DayStatus.COMPLETED }

        val totalValidDays = validDays.size
        return if (totalValidDays > 0) {
            ((completedDays + (0.5)) / totalValidDays) * 100
        } else 0.0
    }

    private fun generateSampleHabitData(): List<CalendarDay> {
        val today = LocalDate.now()
        return (0..59).map { index ->
            val date = today.minusDays(index.toLong())
            CalendarDay(
                date = date,
                DayType.CURRENT,
                status = when ((1..3).random()) {
                    1 -> DayStatus.COMPLETED
                    2 -> DayStatus.MISSED
                    else -> DayStatus.PENDING
                }
            )
        }
    }
}