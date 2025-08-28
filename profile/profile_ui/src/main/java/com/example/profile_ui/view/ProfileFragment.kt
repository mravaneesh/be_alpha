package com.example.profile_ui.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.profile_domain.model.UserProfile
import com.example.profile_ui.R
import com.example.profile_ui.adapter.PostsAdapter
import com.example.profile_ui.adapter.ProfilePagerAdapter
import com.example.profile_ui.databinding.FragmentProfileBinding
import com.example.profile_ui.viewmodel.EditProfileViewModel
import com.example.profile_ui.viewmodel.ProfileViewModel
import com.example.utils.CommonFun
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class ProfileFragment : Fragment() {
    private val viewModel: ProfileViewModel by activityViewModels()
    private val editProfileViewModel: EditProfileViewModel by activityViewModels()
    private lateinit var binding: FragmentProfileBinding
    private val tabTitles = listOf("Posts", "Statistics", "Challenges")
    private val tabIcons = listOf(
        com.example.utils.R.drawable.ic_post_filled,
        com.example.utils.R.drawable.ic_stats_filled,
        com.example.utils.R.drawable.ic_goals_filled
    )

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

        setUpTabs()
        observeProfileData()
    }

    private fun setUpTabs() {

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tab.setIcon(tabIcons[tab.position])
                tab.text = null
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                tab.setIcon(null) // Remove icon
                tab.text = tabTitles[tab.position] // Show text
            }
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        val initialTab = binding.tabLayout.getTabAt(binding.tabLayout.selectedTabPosition)
        initialTab?.setIcon(tabIcons[initialTab.position])
        initialTab?.text = null
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
        Log.i("ProfileFragment", "updateUI: $profile")
        with(binding) {
            profileName.text = profile.name
            profileBio.text = profile.bio
            profileBio.visibility = if (profile.bio.isBlank()) View.GONE else View.VISIBLE
            usernameTitle.text = "@${profile.userName}"
            postsCount.text = String.format(Locale.getDefault(), "%,d", profile.posts)
            followersCount.text = String.format(Locale.getDefault(), "%,d", profile.followers.size)
            followingCount.text = String.format(Locale.getDefault(), "%,d", profile.following.size)

//            // Load profile image using Glide
//            Glide.with(requireContext())
//                .load(profile.profileImageUrl)
//                .placeholder(R.drawable.placeholder_image)
//                .into(profileImage)
        }
    }
}