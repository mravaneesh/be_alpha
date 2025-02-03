package com.example.bealpha_


import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.example.bealpha_.authentication.activity.IntroActivity
import com.example.bealpha_.databinding.ActivityHostBinding
import com.example.create_ui.view.ExploreFragment
import com.example.goal_ui.view.GoalFragment
import com.example.home_ui.HomeFragment
import com.example.profile_ui.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHostBinding
    private lateinit var auth: FirebaseAuth

    private var selectedItem: Int = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        auth = FirebaseAuth.getInstance()
        binding = ActivityHostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()

        binding.ivMoreOptions.setOnClickListener { view ->
            showMoreOptionsDialog(view)
        }
        binding.navHome.setOnClickListener {
            updateSelectedItem(R.id.nav_home)
            loadFragment(HomeFragment()) // Load HomeFragment
        }
       binding.navGoal.setOnClickListener {
            updateSelectedItem(R.id.nav_goal)
            loadFragment(GoalFragment()) // Load SearchFragment
        }
        binding.navExplore.setOnClickListener {
            updateSelectedItem(R.id.nav_explore)
            loadFragment(ExploreFragment()) // Load NotificationsFragment
        }
        binding.navProfile.setOnClickListener {
            updateSelectedItem(R.id.nav_profile)
            loadFragment(ProfileFragment()) // Load NotificationsFragment
        }

        // Load default fragment when activity is created
        if (savedInstanceState == null) {
            updateSelectedItem(R.id.nav_home) // Default selected item
            loadFragment(HomeFragment()) // Default fragment (Home)
        }

    }

    private fun showMoreOptionsDialog(view : View) {
        val popupMenu = PopupMenu(this,view)
        popupMenu.menuInflater.inflate(R.menu.menu_more_options,popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    // Handle edit action
                    true
                }
                R.id.action_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, IntroActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.action_share -> {
                    true
                }
                else -> false
            }
        }

        popupMenu.show() // Show the popup menu
    }

    // Function to update the selected item and change the drawable
    private fun updateSelectedItem(selectedId: Int) {

        // Reset the drawables to unselected icons
        when (selectedItem) {
            R.id.nav_home -> {
                binding.navHome.setImageResource(R.drawable.ic_home)
            }
            R.id.nav_goal -> {
                binding.navGoal.setImageResource(R.drawable.ic_goal)
            }
            R.id.nav_explore -> {
                binding.navExplore.setImageResource(R.drawable.ic_explore)
            }
            R.id.nav_profile -> {
                binding.navProfile.setImageResource(R.drawable.ic_profile)
            }
        }

        // Set the selected item to the new icon
        when (selectedId) {
            R.id.nav_home -> {
                binding.navHome.setImageResource(R.drawable.ic_home_select)
            }
            R.id.nav_goal -> {
                binding.navGoal.setImageResource(R.drawable.ic_goal_select)
            }
            R.id.nav_explore -> {
                binding.navExplore.setImageResource(R.drawable.ic_explore_select)
            }
            R.id.nav_profile -> {
                binding.navProfile.setImageResource(R.drawable.ic_profile_select)
            }
        }

        // Update the current selected item ID
        selectedItem = selectedId
    }

    // Function to load fragments
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
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