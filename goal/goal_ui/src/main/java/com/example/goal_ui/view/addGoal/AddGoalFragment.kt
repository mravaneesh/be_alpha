package com.example.goal_ui.view.addGoal

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.goal_domain.model.Goal
import com.example.goal_ui.R
import com.example.goal_ui.databinding.FragmentAddGoalBinding
import com.example.goal_ui.viewmodel.AddGoalViewModel
import com.example.utils.CommonFun
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class AddGoalFragment : Fragment() {

    private val userId = CommonFun.getCurrentUserId()!!
    private var timesPerWeek = 7
    private var frequency = "Daily"
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
            binding.btnSaveGoal.text = "Add Goal"
            binding.btnSaveGoal.setOnClickListener { saveGoal() }
        }
        return binding.root
    }

    private fun setUpEditMode() {
        val goalId = arguments?.getString("goalId") ?: ""
        val title = arguments?.getString("title") ?: ""
        val description = arguments?.getString("description") ?: ""
        val color = arguments?.getInt("color") ?: R.color.color1
        val frequency = arguments?.getString("frequency") ?: "Daily"
        val selectedDays = arguments?.getInt("selectedDays") ?: 7
        val reminder = arguments?.getString("reminder") ?: ""

        binding.etGoalTitle.setText(title)
        binding.etGoalDescription.setText(description)
        viewModel.setColor(color)
        binding.colorPreview.setBackgroundColor(color)

        if (frequency == "Weekly") {
            binding.tabFrequency.getTabAt(1)?.select() // Select 'Weekly' tab
            binding.weeklyOptions.visibility = View.VISIBLE
            binding.tvTimesPerWeek.text = "$selectedDays times a week"
            binding.tvNumber.text = selectedDays.toString()
        } else {
            binding.tabFrequency.getTabAt(0)?.select() // Select 'Daily' tab
            binding.weeklyOptions.visibility = View.GONE
        }

        if (reminder.isNotEmpty()) {
            binding.switchReminder.isChecked = true
            binding.tvSelectedTime.visibility = View.VISIBLE
            binding.tvSelectedTime.text = reminder
            viewModel.setTime(reminder)
        } else {
            binding.switchReminder.isChecked = false
            binding.tvSelectedTime.visibility = View.GONE
        }
        binding.btnSaveGoal.text = "Update Goal"
        binding.btnSaveGoal.setOnClickListener { updateGoal(goalId) }
    }

    private fun updateGoal(goalId: String) {
        val updatedGoal = Goal(
            id = goalId,
            title = binding.etGoalTitle.text.toString(),
            description = binding.etGoalDescription.text.toString(),
            color = viewModel.selectedColor.value!!
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
                    "color" to updatedGoal.color
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
        setupFrequencyTab()
        setupLayout()
        setupViewmodel()

        viewModel.setColor(ContextCompat.getColor(requireContext(),R.color.color1))
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.selectedColor.observe(viewLifecycleOwner) { selectedColor ->
                binding.colorPreview.setBackgroundColor(selectedColor)
            }
        }
        binding.tvTimesPerWeek.text = "$timesPerWeek times a week"
        binding.tvNumber.text = timesPerWeek.toString()
        setupWeekFrequency()

        viewModel.selectedTime.observe(viewLifecycleOwner) { time ->
            binding.tvSelectedTime.text = time
        }
    }

    private fun setupViewmodel() {
        viewModel.setTime(CommonFun.getCurrentTime())
    }

    private fun setupWeekFrequency()
    {
        binding.btnIncrease.setOnClickListener {
            if (timesPerWeek < 6) {
                timesPerWeek++
                binding.tvTimesPerWeek.text = "$timesPerWeek times a week"
                binding.tvNumber.text = timesPerWeek.toString()
            }
        }
        binding.btnDecrease.setOnClickListener {
            if (timesPerWeek > 1) {
                timesPerWeek--
                if(timesPerWeek == 1)
                    binding.tvTimesPerWeek.text = "$timesPerWeek time a week"
                else
                    binding.tvTimesPerWeek.text = "$timesPerWeek times a week"
                binding.tvNumber.text = timesPerWeek.toString()
            }
        }
    }

    private fun setupLayout()
    {
        binding.colorPreview.setOnClickListener {
            val dialog = SelectionDialogFragment().apply {
                arguments = Bundle().apply { putString("selection", "color") }
            }
            dialog.show(parentFragmentManager, "SelectionDialog")
        }

        binding.tvSelectedTime.setOnClickListener {
            val dialog = SelectionDialogFragment().apply {
                arguments = Bundle().apply { putString("selection", "reminder") }
            }
            dialog.show(parentFragmentManager, "SelectionDialog")
        }

        handleReminderSwitch()

        binding.btnSaveGoal.setOnClickListener {
            saveGoal()
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun handleReminderSwitch()
    {
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                binding.tvSelectedTime.visibility = View.VISIBLE
            }
            else{
                binding.tvSelectedTime.apply{
                    visibility = View.GONE
                    text = ""
                }
            }
        }
    }

    private fun setupFrequencyTab() {
        val tabLayout = binding.tabFrequency
        tabLayout.addTab(tabLayout.newTab().setText("Daily"))
        tabLayout.addTab(tabLayout.newTab().setText("Weekly"))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.weeklyOptions.visibility = View.GONE
                    }
                    1 -> {
                        binding.weeklyOptions.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
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
            description, frequency,timesPerWeek,selectedColor ,selectedTime,startDate = startDate )

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
