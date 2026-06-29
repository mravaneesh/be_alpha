package com.example.onboarding_ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.designsystem.theme.PactTheme
import com.example.onboarding_ui.compose.BasicInfoScreen
import com.example.onboarding_ui.viewmodel.OnboardingViewModel
import com.example.utils.CommonFun
import com.google.firebase.firestore.FirebaseFirestore

class BasicInfoFragment : Fragment() {

    private val viewModel: OnboardingViewModel by activityViewModels()
    private var gender by mutableStateOf("Select")
    private var birthday by mutableStateOf("Select")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        gender = viewModel.selectedGender.value ?: "Select"
        birthday = viewModel.selectedBirthday.value ?: "Select"

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PactTheme {
                    BasicInfoScreen(
                        gender = gender,
                        birthday = birthday,
                        onPickGender = ::pickGender,
                        onPickBirthday = ::pickBirthday,
                        onNext = ::next,
                    )
                }
            }
        }
    }

    private fun pickGender() {
        val sheet = BottomSheetSelection.newInstance("gender", gender) { selected ->
            gender = selected
            viewModel.setSelectedGender(selected)
        }
        sheet.show(childFragmentManager, sheet.tag)
    }

    private fun pickBirthday() {
        val sheet = BottomSheetSelection.newInstance("birthday", birthday) { selected ->
            birthday = selected
            viewModel.setSelectedBirthday(selected)
        }
        sheet.show(childFragmentManager, sheet.tag)
    }

    private fun next(heightCm: Int, weightKg: Float) {
        viewModel.setSelectedGender(gender)
        viewModel.setSelectedBirthday(birthday)
        viewModel.setHeightInCm(heightCm)
        viewModel.setWeightInKg(weightKg)
        viewModel.setAge(CommonFun.calculateAge(birthday))

        val userId = CommonFun.getCurrentUserId()!!
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .update(mapOf("gender" to gender, "birthdate" to birthday))
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to collect user data", Toast.LENGTH_SHORT).show()
            }

        (parentFragment as? WelcomeFragment)?.goToNextPage()
    }
}
