package com.example.utils

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar


object CommonFun {

    fun ComponentActivity.applyWindowInsets()
    {
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView.rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,0)
            insets
        }
    }


    fun getCurrentUserId(): String? {
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        return currentUser?.uid // Returns the User ID if logged in, otherwise null
    }

    fun getCurrentTime():String
    {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return formatTime(hour,minute)
    }

    fun deepLinkNav(destination: String, navController: NavController) {
        val deepLinkRequest = NavDeepLinkRequest.Builder
            .fromUri(destination.toUri())
            .build()

        navController.navigate(deepLinkRequest)
    }


    private fun formatTime(hour: Int, minute: Int): String {
        val formattedHour = if (hour > 12) hour - 12 else hour
        val amPm = if (hour >= 12) "PM" else "AM"
        return String.format("%02d:%02d %s", formattedHour, minute, amPm)
    }
}