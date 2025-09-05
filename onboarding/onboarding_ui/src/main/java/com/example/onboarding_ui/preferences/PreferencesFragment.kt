package com.example.onboarding_ui.preferences

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onboarding_domain.model.UserPreferences
import com.example.onboarding_ui.databinding.FragmentPreferencesBinding
import com.example.onboarding_ui.preferences.model.PreferenceItem
import com.example.onboarding_ui.preferences.model.PreferenceSection
import com.example.onboarding_ui.view.WelcomeFragment
import com.example.onboarding_ui.viewmodel.OnboardingViewModel
import com.example.utils.CommonFun
import com.example.utils.Resource

class PreferencesFragment : Fragment() {
    private lateinit var binding:FragmentPreferencesBinding
    private val viewModel: OnboardingViewModel by activityViewModels()
    private lateinit var adapter: PreferencesAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPreferencesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sections = listOf(
            PreferenceSection(
                "Fitness Goals",
                listOf(
                    PreferenceItem("1", "Lose Weight", "💪"),
                    PreferenceItem("2", "Build Muscle", "🏋️"),
                    PreferenceItem("3", "Stay Fit", "🤸"),
                    PreferenceItem("4", "Boost Strength", "⚡"),
                    PreferenceItem("5", "Improve Flexibility", "🧘"),
                    PreferenceItem("6", "Increase Endurance", "🏃"),
                    PreferenceItem("7", "Tone Body", "🔥")
                ),
                isMultiSelect = true
            ),
            PreferenceSection(
                "Diet Preferences",
                listOf(
                    PreferenceItem("8", "Vegetarian", "🥦"),
                    PreferenceItem("9", "Vegan", "🌱"),
                    PreferenceItem("10", "Keto", "🥩"),
                    PreferenceItem("11", "Balanced", "🍲"),
                    PreferenceItem("12", "Low Carb", "🥑"),
                    PreferenceItem("13", "Paleo", "🍖"),
                    PreferenceItem("14", "High Protein", "🍗"),
                    PreferenceItem("15", "Intermittent Fasting", "⏰")
                ),
                isMultiSelect = true
            ),
            PreferenceSection(
                "Workout Style",
                listOf(
                    PreferenceItem("16", "Gym", "🏋️"),
                    PreferenceItem("17", "Home", "🏠"),
                    PreferenceItem("18", "Running", "🏃"),
                    PreferenceItem("19", "Yoga", "🧘"),
                    PreferenceItem("20", "Pilates", "🤸"),
                    PreferenceItem("21", "CrossFit", "💥"),
                    PreferenceItem("22", "Cycling", "🚴"),
                    PreferenceItem("23", "Swimming", "🏊"),
                    PreferenceItem("24", "Sports", "⚽")
                ),
                isMultiSelect = true
            ),
            PreferenceSection(
                "Habit Tracking",
                listOf(
                    PreferenceItem("25", "Daily Steps", "👟"),
                    PreferenceItem("26", "Sleep Tracking", "😴"),
                    PreferenceItem("27", "Water Intake", "💧"),
                    PreferenceItem("28", "Journaling", "📓"),
                    PreferenceItem("29", "Mindfulness / Meditation", "🧘"),
                    PreferenceItem("30", "Calorie Tracking", "🍎"),
                    PreferenceItem("31", "Weekly Progress Check", "📊")
                ),
                isMultiSelect = true
            ),
            PreferenceSection(
                "Workout Time Preference",
                listOf(
                    PreferenceItem("37", "Morning", "🌅"),
                    PreferenceItem("38", "Afternoon", "🌞"),
                    PreferenceItem("39", "Evening", "🌆"),
                    PreferenceItem("40", "Flexible", "🔄")
                ),
                isMultiSelect = false
            )
        )
        adapter = PreferencesAdapter(sections) {section,item ->
            Log.i("PreferencesFragment", "onViewCreated: $section $item")
            binding.btnNext.isEnabled = adapter.isAllSectionsSelected()
        }
        setupOnClick()
        setUpObserver()
        binding.rvSections.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSections.adapter = adapter
    }

    private fun setupOnClick() {
        binding.btnprev.setOnClickListener {
            (parentFragment as? WelcomeFragment)?.goToPreviousPage()
        }
        binding.btnNext.setOnClickListener {
            (parentFragment as? WelcomeFragment)?.goToNextPage()
            val selectedPrefs = adapter.getSelectedPreferences()
            val prefs = UserPreferences(
                userId = CommonFun.getCurrentUserId()!!,
                gender = viewModel.selectedGender.value ?: "",
                age = viewModel.age.value,
                heightCm = viewModel.heightInCm.value ?: 0,
                weightKg = viewModel.weightInKg.value ?: 0f,
                fitnessGoal = selectedPrefs["Fitness Goals"] ?: emptyList(),
                dietType = selectedPrefs["Diet Preferences"] ?: emptyList(),
                workoutStyle = selectedPrefs["Workout Style"] ?: emptyList(),
                habitsTrack = selectedPrefs["Habit Tracking"] ?: emptyList(),
                workoutTime = selectedPrefs["Workout Time Preference"]?.firstOrNull() ?: ""
            )
            viewModel.saveUserPreferences(prefs)
        }
    }

    private fun setUpObserver() {
        viewModel.saveState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.lottieProgress.visibility = View.VISIBLE
                    binding.btnNext.visibility = View.GONE
                    binding.btnprev.isEnabled = false
                }
                is Resource.Success -> {
                    binding.lottieProgress.visibility = View.GONE
                    binding.btnNext.visibility = View.VISIBLE
                    binding.btnprev.isEnabled = false
                }
                is Resource.Error -> {
                    binding.lottieProgress.visibility = View.GONE
                    binding.btnNext.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    binding.btnprev.isEnabled = false
                }
            }
        }
    }
}