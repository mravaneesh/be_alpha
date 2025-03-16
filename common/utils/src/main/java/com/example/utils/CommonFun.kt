package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

    fun deepLinkNav(destination: String, context:Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("bealpha://app/$destination"))
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        context.startActivity(intent)
    }

    fun View.applyScaleAnimation() {
        setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(150).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                    if (event.action == MotionEvent.ACTION_UP) {
                        view.performClick() // Important for accessibility
                    }
                }
            }
            false
        }
    }

    fun passwordVisibility(ivEye: View, etPassword: EditText) {
        var isPasswordVisible = false
        ivEye.setOnClickListener {
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivEye.setBackgroundResource(R.drawable.show_password)
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivEye.setBackgroundResource(R.drawable.hide_password)
            }

            etPassword.setSelection(etPassword.text.length)
            isPasswordVisible = !isPasswordVisible
        }
    }
    private fun formatTime(hour: Int, minute: Int): String {
        val formattedHour = if (hour > 12) hour - 12 else hour
        val amPm = if (hour >= 12) "PM" else "AM"
        return String.format("%02d:%02d %s", formattedHour, minute, amPm)
    }


    fun getScreenSize(context: Context): Point {
        val wm = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
        val metrics = wm.maximumWindowMetrics

        return Point(metrics.bounds.width(), metrics.bounds.height())

    }
}