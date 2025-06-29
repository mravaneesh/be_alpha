package com.example.goal_ui.view.addGoal

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.goal_domain.model.Goal
import com.example.goal_ui.R
import com.example.goal_ui.databinding.FragmentAddGoalBinding
import com.example.goal_ui.viewmodel.AddGoalViewModel
import com.example.utils.CommonFun
import com.google.android.material.chip.Chip

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class AddGoalFragment : Fragment() {

    private val userId = CommonFun.getCurrentUserId()!!
    private val selectedDays = mutableSetOf(0, 1, 2, 3, 4, 5, 6)
    private var category = "Habit"
    private var _binding: FragmentAddGoalBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddGoalViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddGoalBinding.inflate(inflater, container, false)
        setupFragment()

        val isEditMode = arguments?.getBoolean("isEditMode", false) ?: false
        if (isEditMode) {
            setUpEditMode()
        } else {
            binding.btnSaveGoal.text = "Add"
            binding.btnSaveGoal.setOnClickListener { saveGoal() }
        }
        return binding.root
    }

    private fun setUpEditMode() {
        val goalId = arguments?.getString("goalId") ?: ""
        val title = arguments?.getString("title") ?: ""
        val description = arguments?.getString("description") ?: ""
        val color = arguments?.getInt("color") ?: R.color.color1
        val savedSelectedDays = arguments?.getIntegerArrayList("selectedDays") ?: arrayListOf()
        val reminder = arguments?.getString("reminder") ?: ""

        binding.etGoalTitle.setText(title)
        binding.etGoalDescription.setText(description)
        viewModel.setColor(color)
        selectedDays.clear()
        selectedDays.addAll(savedSelectedDays)
        updateDaySelectionUI()

        if (reminder.isNotEmpty()) {
            binding.switchReminder.isChecked = true
            binding.tvSelectedTime.visibility = View.VISIBLE
            binding.icDropDown.visibility = View.VISIBLE
            binding.tvSelectedTime.text = reminder
            viewModel.setTime(reminder)
            val (hour, minute) = parseTimeToHourMinute(reminder)
            binding.timePicker.hour = hour
            binding.timePicker.minute = minute
        } else {
            binding.switchReminder.isChecked = false
            binding.tvSelectedTime.visibility = View.GONE
            binding.icDropDown.visibility = View.GONE
        }
        binding.btnSaveGoal.text = "Update"
        binding.toolbar.title = "Update Habit"
        binding.btnSaveGoal.setOnClickListener { updateGoal(goalId) }
    }

    private fun parseTimeToHourMinute(time: String): Pair<Int, Int> {
        val formatter = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        val date = formatter.parse(time)
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date!!
        return Pair(calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE))
    }


    private fun updateGoal(goalId: String) {
        val updatedGoal = Goal(
            id = goalId,
            title = binding.etGoalTitle.text.toString(),
            description = binding.etGoalDescription.text.toString(),
            color = viewModel.selectedColor.value!!,
            selectedDays = selectedDays.toList()
        )

        FirebaseFirestore.getInstance()
            .collection("goals")
            .document(userId)
            .collection("Habit")
            .document(goalId)
            .update(
                mapOf(
                    "title" to updatedGoal.title,
                    "description" to updatedGoal.description,
                    "color" to updatedGoal.color,
                    "selectedDays" to updatedGoal.selectedDays
                )
            )
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Goal updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update goal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupFragment() {
        setupDaySelection()
        setupLayout()
        setupViewmodel()

        viewModel.setColor(ContextCompat.getColor(requireContext(),R.color.color1))

        viewModel.selectedTime.observe(viewLifecycleOwner) { time ->
            binding.tvSelectedTime.text = time
        }
    }

    private fun setupViewmodel() {
        viewModel.setTime(CommonFun.getCurrentTime())
    }
    private fun setupDaySelection() {
        binding.linearLayoutDays.children.forEach { view ->
            val textView = view as TextView
            textView.isSelected = true
            setDaySelectionUI(textView, true)

            textView.setOnClickListener {
                val dayIndex = textView.tag.toString().toInt()

                if (selectedDays.contains(dayIndex)) {
                    selectedDays.remove(dayIndex)
                    setDaySelectionUI(textView, false)
                } else {
                    selectedDays.add(dayIndex)
                    setDaySelectionUI(textView, true)
                }
            }
        }
    }

    private fun setDaySelectionUI(textView: TextView, isSelected: Boolean) {
        textView.isSelected = isSelected
        textView.setTextColor(
            ContextCompat.getColor(requireContext(), if (isSelected) R.color.black
            else R.color.unselect_day)
        )
    }

    private fun setupLayout()
    {
        var isExpanded = false
        binding.icDropDown.setOnClickListener {
            isExpanded = !isExpanded
            val rotation = if (isExpanded) 180f else 0f
            binding.icDropDown.animate().rotation(rotation).setDuration(300).start()
            binding.timePicker.isVisible = isExpanded
        }

        handleReminderSwitch()

        binding.btnSaveGoal.setOnClickListener {
            saveGoal()
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            val formattedTime = CommonFun.formatTime(hourOfDay, minute)
            viewModel.setTime(formattedTime)
        }
    }

    private fun handleReminderSwitch()
    {
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                binding.tvSelectedTime.visibility = View.VISIBLE
                binding.icDropDown.visibility = View.VISIBLE
            }
            else{
                binding.tvSelectedTime.visibility = View.GONE
                binding.icDropDown.visibility = View.GONE
            }
        }
    }


    private fun saveGoal() {
        val description = binding.etGoalDescription.text.toString()
        val title = binding.etGoalTitle.text.toString()
        val selectedColor = viewModel.selectedColor.value!!
        val selectedTime = viewModel.selectedTime.value!!

        saveGoalToFirestore(category,description,title,selectedColor,selectedTime)
        findNavController().popBackStack()
    }

    private fun saveGoalToFirestore(category: String, description:String,
                                    title: String,selectedColor:Int, selectedTime:String) {
        val db = FirebaseFirestore.getInstance()
        val goalId = UUID.randomUUID().toString()
        val startDate = LocalDate.now().toString()
        val goalDb = Goal(goalId, category, title,
            description, selectedDays.toList(),selectedColor,selectedTime,startDate )

        db.collection("goals")
            .document(userId)
            .collection(category)
            .document(goalId)
            .set(goalDb)
            .addOnSuccessListener {
                Log.d("Firestore", "Goal successfully added")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding goal", e)
            }
    }

    private fun updateDaySelectionUI() {
        binding.linearLayoutDays.children.forEach { view ->
            val textView = view as TextView
            val dayIndex = textView.tag.toString().toInt()
            textView.isSelected = selectedDays.contains(dayIndex)
            setDaySelectionUI(textView, textView.isSelected)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
