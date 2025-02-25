package com.example.profile_ui.view.editProfile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.profile_domain.model.UserProfile
import com.example.profile_ui.R
import com.example.profile_ui.databinding.FragmentEditProfileBinding
import com.example.profile_ui.viewmodel.EditProfileViewModel
import com.example.profile_ui.viewmodel.ProfileViewModel
import com.example.utils.CommonFun
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class EditProfileFragment : Fragment() {
    private lateinit var binding: FragmentEditProfileBinding
    private val viewModel: EditProfileViewModel by activityViewModels()
    private val firestore = FirebaseFirestore.getInstance()
    private val userId = CommonFun.getCurrentUserId()!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        setupClickListeners()
        observeViewModel()

        binding.btnUpdateProfile.setOnClickListener {
            updateUserProfile()
        }

        return binding.root
    }

    private fun setupClickListeners() {
        binding.selectGender.setOnClickListener {
            val bottomSheet = BottomSheetFragment.newInstance("gender",
                viewModel.selectedGender.value!!){ selectedGender ->
                binding.selectGender.text = selectedGender
                viewModel.setSelectedGender(selectedGender)
            }
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }

        binding.selectBday.setOnClickListener {
            val bottomSheet = BottomSheetFragment.newInstance("birthday",
                viewModel.selectedBirthday.value!!){ selectedBirthday ->
                binding.selectBday.text = selectedBirthday
                viewModel.setSelectedBirthday(selectedBirthday)
            }
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewModel.editUser.observe(viewLifecycleOwner) { user ->
            updateUI(user)
        }

        viewModel.selectedGender.observe(viewLifecycleOwner) { gender ->
            binding.selectGender.text = gender
        }

        viewModel.selectedBirthday.observe(viewLifecycleOwner) { birthday ->
            binding.selectBday.text = birthday
        }
    }

    private fun updateUI(profile: UserProfile) {
        with(binding) {
            etUsername.setText(profile.userName)
            etName.setText(profile.name)
            etBio.setText(profile.bio)
            selectGender.text = profile.gender
            selectBday.text = profile.birthdate

            viewModel.setSelectedGender(profile.gender)
            viewModel.setSelectedBirthday(profile.birthdate)
//            // Load profile image using Glide
//            Glide.with(requireContext())
//                .load(profile.profileImageUrl)
//                .placeholder(R.drawable.placeholder_image)
//                .into(profileImage)
        }
    }
    private fun updateUserProfile() {
        val username = binding.etUsername.text.toString()
        val name = binding.etName.text.toString()
        val bio = binding.etBio.text.toString()
        val gender = binding.selectGender.text.toString()
        val birthday = binding.selectBday.text.toString()

        val userProfileUpdate = mapOf(
            "name" to name,
            "username" to username,
            "bio" to bio,
            "gender" to gender,
            "birthdate" to birthday
        )

        firestore.collection("users").document(userId).update(userProfileUpdate)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile Updated", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update user data", Toast.LENGTH_SHORT).show()
            }
    }
}