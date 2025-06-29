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
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
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
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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
                            updateTextWithAnimation(binding.successRate, filteredGoal.successRate,"%")
                            updateTextWithAnimation(binding.totalDays, filteredGoal.totalCompleted)
                        } ?: Log.e("GoalFilter", "Goal with ID ${goal.id} not found")
                    }
                }
            }
        }
    }

    private fun updateTextWithAnimation(textView: TextView, targetValue: Int, suffix: String ="") {
        val formattedValue = String.format(Locale.getDefault(), "%d%s", targetValue, suffix)

        if (textView.text.toString() == formattedValue) return // Skip animation if value hasn't changed
        textView.text = formattedValue
        val slideUp = AnimationUtils.loadAnimation(textView.context, com.example.utils.R.anim.slide_up)
        textView.startAnimation(slideUp)
    }

    private fun initCalendar() {
        Log.i("HabitAnalyticsFragment", "GoalCalendar: $goal")
        val calendarFragment = CalendarFragment().apply {
            arguments = Bundle().apply {
                putParcelable("goal", goal)
            }
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.calendarContainer, calendarFragment)
            .commit()
    }

    private fun displaySuccessRateChart() {
        val chart: LineChart = binding.lineChart
        val dates = listOf("Jun 20", "Jun 21", "Jun 22", "Jun 23", "Jun 24","Jun 25","Jun 26","Jun 27","Jun 28","Jun 29","Jun 30")
        val successRates = listOf(0f, 60f, 90f, 100f, 70f,60f,80f,90f,100f,70f,80f)
        setupSuccessRateChart(
            chart = chart,
            dateLabels = dates,
            successRates = successRates,
            habitName = "Meditate",
            color = ContextCompat.getColor(requireContext(), R.color.button_primary)
        )
    }

    private fun setupSuccessRateChart(
        chart: LineChart,
        dateLabels: List<String>,        // e.g., ["Jun 20", "Jun 21", ...]
        successRates: List<Float>,       // e.g., [80f, 60f, 100f, ...]
        habitName: String,               // e.g., "Meditate"
        color: Int                       // e.g., ContextCompat.getColor(context, R.color.teal_700)
    ) {

        val entries = successRates.mapIndexed { index, value ->
            Entry(index.toFloat(), value)
        }

        val dataSet = LineDataSet(entries, habitName).apply {
            this.color = color
            setCircleColor(color)
            lineWidth = 2f
            circleRadius = 4f
            valueTextSize = 10f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            setDrawValues(false)
            fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.line_shadow)
            setDrawHorizontalHighlightIndicator(false)
            setDrawVerticalHighlightIndicator(false)
        }

        val limitLine = LimitLine(100f).apply {
            lineColor = R.color.cool_gray
            lineWidth = 1f
            enableDashedLine(10f, 10f, 0f)
            label = ""
        }
        chart.apply {
            data = LineData(dataSet)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                isGranularityEnabled = true
                valueFormatter = IndexAxisValueFormatter(dateLabels)
                setDrawGridLines(false)
                setDrawAxisLine(false)
                labelRotationAngle = 0f
                textSize = 12f
                typeface = ResourcesCompat.getFont(requireContext(),R.font.raleway_regular)
                setAvoidFirstLastClipping(false)
                axisMinimum = -1f
                axisMaximum = (dateLabels.size+1).toFloat()
            }

            axisLeft.apply {
                isEnabled = true
                axisMinimum = -10f
                granularity = 10f
                isGranularityEnabled = true
                textSize = 12f
                typeface = ResourcesCompat.getFont(requireContext(),R.font.raleway_regular)
                axisMaximum = 110f
                setDrawGridLines(false)
                setDrawAxisLine(false)
                addLimitLine(limitLine)
            }
            axisLeft.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if(value == 110f || value == -10f) "" else "${value.toInt()}%"
                }
            }

            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false

            setVisibleXRangeMaximum(6f)
            isDragEnabled = true
            setScaleEnabled(false)

            dragDecelerationFrictionCoef = 0.9f
            setExtraOffsets(0f, 0f, 0f, 0f)

            moveViewToX(successRates.size.toFloat()) // start at end
            invalidate()
        }
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