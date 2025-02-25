package com.example.bealpha_


import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.bealpha_.databinding.ActivityHostBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHostBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView

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
    }

    private fun setupNavigation(savedInstanceState: Bundle?)
    {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        bottomNavigationView = binding.bottomNavigation

        if (savedInstanceState == null) {
            val isUserLoggedIn = FirebaseAuth.getInstance().currentUser != null
            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
            navGraph.setStartDestination(
                if (isUserLoggedIn) R.id.home_nav_graph else com.example.authentication.R.id.auth_nav_graph
            )
            navController.graph = navGraph
        }
        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNavigationView.visibility = if (destination.id in startDestinations) {
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