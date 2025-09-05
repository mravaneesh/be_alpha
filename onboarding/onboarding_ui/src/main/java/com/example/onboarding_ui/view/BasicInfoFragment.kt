package com.example.onboarding_ui.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.utils.R as R
import com.example.onboarding_ui.databinding.FragmentBasicInfoBinding
import com.example.onboarding_ui.viewmodel.OnboardingViewModel
import com.example.utils.CommonFun
import com.example.utils.CommonFun.afterTextChanged
import com.example.utils.Prefs
import com.google.firebase.firestore.FirebaseFirestore


class BasicInfoFragment : Fragment() {
    private var _binding: FragmentBasicInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OnboardingViewModel by activityViewModels()
    private var isCmSelected = true
    private var isKgSelected = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBasicInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toggleUnits()
        setupClickListeners()
        observeViewModel()
        binding.nextButton.isEnabled = false
        setupTextWatchers()

        binding.nextButton.setOnClickListener {
            updateUser()
            (parentFragment as? WelcomeFragment)?.goToNextPage()
        }
    }

    private fun observeViewModel() {
        viewModel.selectedGender.observe(viewLifecycleOwner) { gender ->
            binding.selectGender.text = gender
            validateFields()
        }

        viewModel.selectedBirthday.observe(viewLifecycleOwner) { birthday ->
            binding.selectBday.text = birthday
            validateFields()
        }
    }

    private fun toggleUnits() {
        binding.tvCm.setOnClickListener {
            if (!isCmSelected) {
                binding.tvCm.setBackgroundResource(R.drawable.day_selected_background)
                binding.tvFtIn.setBackgroundResource(R.drawable.day_unselected_background)
                binding.etCm.visibility = View.VISIBLE
                binding.ftIn.visibility = View.GONE
                isCmSelected = true
            }
        }

        binding.tvFtIn.setOnClickListener {
            if (isCmSelected) {
                binding.tvFtIn.setBackgroundResource(R.drawable.day_selected_background)
                binding.tvCm.setBackgroundResource(R.drawable.day_unselected_background)
                binding.ftIn.visibility = View.VISIBLE
                binding.etCm.visibility = View.GONE
                isCmSelected = false
            }
        }

        binding.tvKg.setOnClickListener {
            if (!isKgSelected) {
                binding.tvKg.setBackgroundResource(R.drawable.day_selected_background)
                binding.tvLbs.setBackgroundResource(R.drawable.day_unselected_background)
                binding.etWeight.hint = "Weight (kg)"
                isKgSelected = true
            }
        }

        binding.tvLbs.setOnClickListener {
            if (isKgSelected) {
                binding.tvLbs.setBackgroundResource(R.drawable.day_selected_background)
                binding.tvKg.setBackgroundResource(R.drawable.day_unselected_background)
                binding.etWeight.hint = "Weight (lbs)"
                isKgSelected = false
            }
        }
    }

//    private fun updateUI() {
//        Log.i("BasicInfoFragment", "updateUI")
//        FirebaseFirestore.getInstance().collection("users")
//            .document(CommonFun.getCurrentUserId()!!).get()
//            .addOnSuccessListener { document ->
//                Log.i("BasicInfoFragment", "updateUI: $document")
//                if (document != null && document.exists()) {
//                    val gender = document.getString("gender") ?: ""
//                    val birthDate = document.getString("birthdate") ?: ""
//                    viewModel.setSelectedGender(gender)
//                    viewModel.setSelectedBirthday(birthDate)
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e("Firestore", "Error fetching user data", e)
//            }
//    }

    private fun setupClickListeners() {
        binding.selectGender.setOnClickListener {
            val bottomSheet = BottomSheetSelection.newInstance("gender",
                viewModel.selectedGender.value!!){ selectedGender ->
                binding.selectGender.text = selectedGender
                viewModel.setSelectedGender(selectedGender)
            }
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }

        binding.selectBday.setOnClickListener {
            val bottomSheet = BottomSheetSelection.newInstance("birthday",
                viewModel.selectedBirthday.value!!){ selectedBirthday ->
                binding.selectBday.text = selectedBirthday
                viewModel.setSelectedBirthday(selectedBirthday)
            }
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }
    }

    private fun updateUser() {
        val gender = binding.selectGender.text.toString()
        val birthday = binding.selectBday.text.toString()
        val heightInCm = if (isCmSelected) {
            binding.etCm.text.toString().toIntOrNull() ?: 0
        } else {
            val feet = binding.etFeet.text.toString().toIntOrNull() ?: 0
            val inches = binding.etInch.text.toString().toIntOrNull() ?: 0
            ((feet * 12) + inches) * 2.54
        }.toInt()
        val weightInKg = if (isKgSelected) {
            binding.etWeight.text.toString().toFloatOrNull() ?: 0f
        } else {
            val lbs = binding.etWeight.text.toString().toFloatOrNull() ?: 0f
            lbs * 0.453592f
        }
        val age = CommonFun.calculateAge(birthday)
        viewModel.setSelectedGender(gender)
        viewModel.setSelectedBirthday(birthday)
        viewModel.setHeightInCm(heightInCm)
        viewModel.setWeightInKg(weightInKg)
        viewModel.setAge(age)

        val userId = CommonFun.getCurrentUserId()!!

        val userProfileUpdate = mapOf(
            "gender" to gender,
            "birthdate" to birthday
        )
        FirebaseFirestore.getInstance().collection("users").document(userId).update(userProfileUpdate)
            .addOnSuccessListener {

            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to collect user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateFields() {
        val gender = binding.selectGender.text.toString().isNotBlank()
        val birthday = binding.selectBday.text.toString().isNotBlank()

        val heightFilled = if (isCmSelected) {
            binding.etCm.text.toString().isNotBlank()
        } else {
            binding.etFeet.text.toString().isNotBlank() && binding.etInch.text.toString().isNotBlank()
        }

        val weightFilled = binding.etWeight.text.toString().isNotBlank()

        binding.nextButton.isEnabled = gender && birthday && heightFilled && weightFilled
    }

    private fun setupTextWatchers() {
        binding.apply {
            etCm.afterTextChanged { validateFields() }
            etFeet.afterTextChanged { validateFields() }
            etInch.afterTextChanged { validateFields() }
            etWeight.afterTextChanged { validateFields() }
        }
    }
}