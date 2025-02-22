package com.example.goal_ui.view.addGoal

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.goal_domain.model.Goal
import com.example.goal_ui.R
import com.example.goal_ui.databinding.FragmentAddGoalBinding
import com.example.utils.CommonFun
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class AddGoalFragment : Fragment() {

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
        binding.btnSaveGoal.setOnClickListener { saveGoal() }
        updateToggleView()
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

        return binding.root
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
        val bottomSheetFragment = BottomSheetFragment()
        val bundle = Bundle()

        binding.colorPreview.setOnClickListener {
            bundle.putString("selection", "color")
            bottomSheetFragment.arguments = bundle
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }

        binding.tvSelectedTime.setOnClickListener {
            bundle.putString("selection", "reminder")
            bottomSheetFragment.arguments = bundle
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }

        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                binding.tvSelectedTime.visibility = View.VISIBLE
            }
            else{
                binding.tvSelectedTime.visibility = View.GONE
            }
        }

        binding.btnSaveGoal.setOnClickListener {
            saveGoal()
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }
    private fun updateToggleView() {
        styleSelectedButton(binding.btnDaily)
        styleUnselectedButton(binding.btnWeekly)

        binding.toggleBtn2.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnDaily -> {
                        frequency = "Daily"
                        styleSelectedButton(binding.btnDaily)
                        styleUnselectedButton(binding.btnWeekly)
                        binding.weeklyOptions.visibility = View.GONE
                    }
                    R.id.btnWeekly -> {
                        frequency = "Weekly"
                        binding.weeklyOptions.visibility = View.VISIBLE
                        styleUnselectedButton(binding.btnDaily)
                        styleSelectedButton(binding.btnWeekly)
                    }
                }
            }
        }
    }

    private fun styleSelectedButton(button: MaterialButton) {
        button.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.active_button)
        button.elevation = 10f  // Add elevation when selected
        button.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.black))
        button.setTypeface(null,Typeface.BOLD)
    }

    private fun styleUnselectedButton(button: MaterialButton) {
        button.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.transparent)
        button.elevation = 0f  // Add elevation when selected
        button.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.button_secondary))
        button.setTypeface(null,Typeface.NORMAL)
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
        val goalDb = Goal(goalId, category, title,
            description, frequency,timesPerWeek,selectedColor ,selectedTime )
        val userId = CommonFun.getCurrentUserId()!!

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
