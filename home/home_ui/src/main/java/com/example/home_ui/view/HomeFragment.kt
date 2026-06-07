package com.example.home_ui.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.home_ui.R
import com.example.home_ui.adapter.HomePagerAdapter
import com.example.home_ui.databinding.FragmentFeedBinding
import com.example.home_ui.databinding.FragmentHomeBinding
import com.example.utils.CommonFun.getUser
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HomePagerAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFragment()
    }

    private fun setupFragment() {
        setupViewPagerAndTabs()
        lifecycleScope.launch {
            val user = getUser()
            Log.i("HomeFragment", "setupFragment: $user")
            user?.name?.let { fullName ->
                Log.i("HomeFragment", "setupFragment: $fullName")
                val firstName = fullName.trim().split(" ").firstOrNull() ?: "there"
                binding.tvWelcome.text = "Hi, $firstName 👋"
            }
        }
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val today = Calendar.getInstance().time
        val formattedDate = "Today, ${dateFormat.format(today)}"
        binding.tvDate.text = formattedDate

        binding.ivAddPost.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_habitSelectFragment)
        }
        binding.ivNotification.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_notificationFragment)
        }
    }

    private fun setupViewPagerAndTabs() {
        adapter = HomePagerAdapter(this)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "DashBoard"
                1 -> tab.text = "Feed"
            }
        }.attach()
    }

}