package com.example.profile_ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.profile_domain.model.UserProfile
import com.example.profile_ui.R
import com.example.profile_ui.adapter.ProfilePagerAdapter
import com.example.profile_ui.databinding.FragmentProfileBinding
import com.example.profile_ui.viewmodel.EditProfileViewModel
import com.example.profile_ui.viewmodel.ProfileViewModel
import com.example.utils.CommonFun
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class ProfileFragment : Fragment() {
    private val viewModel: ProfileViewModel by activityViewModels()
    private val editProfileViewModel: EditProfileViewModel by activityViewModels()
    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater,container,false)

        binding.editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }
        binding.settingsIcon.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }

        if (binding.profileBio.text.isNotEmpty()) {
            binding.profileBio.visibility = View.VISIBLE
        } else {
            binding.profileBio.visibility = View.GONE
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = CommonFun.getCurrentUserId()!!
        viewModel.loadProfile(userId)

        val adapter = ProfilePagerAdapter(this)
        binding.viewPager.adapter = adapter

        // Attach TabLayout with ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Posts"
                1 -> "Statistics"
                2 -> "Challenges"
                else -> "Tab $position"
            }
        }.attach()
        observeProfileData()
    }
    private fun observeProfileData() {
        lifecycleScope.launch {
            viewModel.profile.collectLatest { state ->
                when {
                    state.isLoading -> {
                        binding.lottieProgress.visibility = View.VISIBLE
                        binding.linearLayout.visibility = View.GONE
                    }
                    state.error.isNotBlank() -> {
                        binding.lottieProgress.visibility = View.GONE
                        binding.linearLayout.visibility = View.GONE
                    }
                    else -> {
                        val profile = state.profile
                        updateUI(profile)
                        updateEditProfileViewModel(profile)
                        binding.lottieProgress.visibility = View.GONE
                        binding.linearLayout.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun updateEditProfileViewModel(profile: UserProfile) {
        editProfileViewModel.setUserProfile(profile)
    }

    private fun updateUI(profile: UserProfile) {
        with(binding) {
            profileName.text = profile.name
            profileBio.text = profile.bio
            usernameTitle.text = profile.userName
            postsCount.text = String.format(Locale.getDefault(), "%,d", profile.posts)
            followersCount.text = String.format(Locale.getDefault(), "%,d", profile.followers)
            followingCount.text = String.format(Locale.getDefault(), "%,d", profile.following)

//            // Load profile image using Glide
//            Glide.with(requireContext())
//                .load(profile.profileImageUrl)
//                .placeholder(R.drawable.placeholder_image)
//                .into(profileImage)
        }
    }
}