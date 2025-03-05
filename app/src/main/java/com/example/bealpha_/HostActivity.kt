package com.example.bealpha_


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.utils.R
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.bealpha_.databinding.ActivityHostBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHostBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController

    private val startDestinations = setOf(
        com.example.goal_ui.R.id.goalFragment,
        com.example.home_ui.R.id.homeFragment,
        com.example.create_ui.R.id.exploreFragment,
        com.example.profile_ui.R.id.profileFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        auth = FirebaseAuth.getInstance()
        binding = ActivityHostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()
        setupNavigation(savedInstanceState)
        setupBottomNav()
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        navController.handleDeepLink(intent)
    }

    private fun setupBottomNav() {
        binding.bottomNav.navHome.setOnClickListener { setActiveTab("home") }
        binding.bottomNav.navHabit.setOnClickListener { setActiveTab("habit") }
        binding.bottomNav.navTrack.setOnClickListener { setActiveTab("track") }
        binding.bottomNav.navProfile.setOnClickListener { setActiveTab("profile") }
        
        setActiveTab("home")
    }

    private fun setActiveTab(selectedTab: String) {
        when (selectedTab) {
            "home" -> {
                binding.bottomNav.navHome.setImageResource(R.drawable.ic_home_filled)
                binding.bottomNav.navHabit.setImageResource(R.drawable.ic_habit)
                binding.bottomNav.navTrack.setImageResource(R.drawable.ic_explore)
                binding.bottomNav.navProfile.setImageResource(R.drawable.ic_profile)
                navController.navigate(com.example.bealpha_.R.id.home_nav_graph)
            }

            "habit" -> {
                binding.bottomNav.navHome.setImageResource(R.drawable.ic_home)
                binding.bottomNav.navHabit.setImageResource(R.drawable.ic_habit_filled)
                binding.bottomNav.navTrack.setImageResource(R.drawable.ic_explore)
                binding.bottomNav.navProfile.setImageResource(R.drawable.ic_profile)
                navController.navigate(com.example.bealpha_.R.id.goal_nav_graph)
            }

            "track" -> {
                binding.bottomNav.navHome.setImageResource(R.drawable.ic_home)
                binding.bottomNav.navHabit.setImageResource(R.drawable.ic_habit)
                binding.bottomNav.navTrack.setImageResource(R.drawable.ic_explore_filled)
                binding.bottomNav.navProfile.setImageResource(R.drawable.ic_profile)
                navController.navigate(com.example.bealpha_.R.id.create_nav_graph)
            }

            "profile" -> {
                binding.bottomNav.navHome.setImageResource(R.drawable.ic_home)
                binding.bottomNav.navHabit.setImageResource(R.drawable.ic_habit)
                binding.bottomNav.navTrack.setImageResource(R.drawable.ic_explore)
                binding.bottomNav.navProfile.setImageResource(R.drawable.ic_profile_filled)
                navController.navigate(com.example.bealpha_.R.id.profile_nav_graph)
            }
        }
    }


     private fun setupNavigation(savedInstanceState: Bundle?) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(com.example.bealpha_.R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        if (savedInstanceState == null && !navController.handleDeepLink(intent)) {
            val isUserLoggedIn = FirebaseAuth.getInstance().currentUser != null
            val navGraph = navController.navInflater.inflate(com.example.bealpha_.R.navigation.nav_graph)
            navGraph.setStartDestination(
                if (isUserLoggedIn) com.example.home_ui.R.id.home_nav_graph else com.example.authentication.R.id.auth_nav_graph
            )
            navController.graph = navGraph
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.root.visibility = if (destination.id in startDestinations) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }


    private fun ComponentActivity.applyWindowInsets()
    {
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView.rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,systemBars.bottom)
            insets
        }
    }
}