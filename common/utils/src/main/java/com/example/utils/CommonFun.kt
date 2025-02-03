package com.example.utils

import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth


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




}