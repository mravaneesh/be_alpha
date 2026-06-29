package com.example.profile_ui.view.editProfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.designsystem.theme.PactTheme
import com.example.profile_ui.compose.EditProfileScreen
import com.example.profile_ui.viewmodel.EditProfileViewModel
import com.example.profile_ui.viewmodel.ProfileViewModel
import com.example.utils.CommonFun
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileFragment : Fragment() {

    private val viewModel: EditProfileViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val firestore = FirebaseFirestore.getInstance()
    private val userId = CommonFun.getCurrentUserId()!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val profile = viewModel.editUser.value

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PactTheme {
                    EditProfileScreen(
                        modifier = Modifier.systemBarsPadding(),
                        initialName = profile?.name ?: "",
                        initialUsername = profile?.userName ?: "",
                        initialBio = profile?.bio ?: "",
                        initialGender = profile?.gender ?: "",
                        initialBirthday = profile?.birthdate ?: "",
                        onSave = ::updateUserProfile,
                        onBack = { findNavController().navigateUp() },
                    )
                }
            }
        }
    }

    private fun updateUserProfile(name: String, username: String, bio: String, gender: String, birthday: String) {
        val update = mapOf(
            "name" to name,
            "username" to username,
            "bio" to bio,
            "gender" to gender,
            "birthdate" to birthday,
        )
        firestore.collection("users").document(userId).update(update)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile Updated", Toast.LENGTH_SHORT).show()
                profileViewModel.refresh(userId)
                findNavController().navigateUp()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update user data", Toast.LENGTH_SHORT).show()
            }
    }
}
