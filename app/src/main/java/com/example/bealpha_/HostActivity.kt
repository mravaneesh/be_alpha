package com.example.bealpha_

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.bealpha_.databinding.ActivityHostBinding
import com.example.utils.Prefs
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHostBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private val isUserLoggedIn = FirebaseAuth.getInstance().currentUser != null
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
        WindowCompat.setDecorFitsSystemWindows(window, false)
        applyWindowInsets()
        setupNavigation(savedInstanceState)
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (::navController.isInitialized) {
            navController.handleDeepLink(intent)
        }
    }

     private fun setupNavigation(savedInstanceState: Bundle?) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        if (savedInstanceState == null && !navController.handleDeepLink(intent)) {
            val navGraph = navController.navInflater.inflate(com.example.bealpha_.R.navigation.nav_graph)
            navGraph.setStartDestination(
                when{
                    !isUserLoggedIn -> com.example.authentication.R.id.auth_nav_graph
                    Prefs.isKeyExists(this) && !Prefs.isOnboardingCompleted(this) ->
                        com.example.onboarding_ui.R.id.onboarding_nav_graph
                    else -> com.example.home_ui.R.id.home_nav_graph
                }
            )
            navController.graph = navGraph
        }
         binding.bottomNavigation.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigation.visibility = if (startDestinations.contains(destination.id)) {
                View.VISIBLE
            } else {
                View.GONE
            }
            if(startDestinations.contains(destination.id)) {

            }
        }
    }
    private fun ComponentActivity.applyWindowInsets()
    {
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView.rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left,0, systemBars.right,0)
            insets
        }
    }
}