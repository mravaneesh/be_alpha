package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import com.example.utils.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
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

    fun getTodayDate():String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "$year-$month-$day"
    }

    fun getCurrentTime():String
    {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return formatTime(hour,minute)
    }

    fun deepLinkNav(destination: String, context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("bealpha://app/$destination"))
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        context.startActivity(intent)
    }


    fun navigateToDeepLinkFragment(
        navController: NavController,
        destination: String
    ) {
        val uri = Uri.parse("bealpha://app/$destination")
        navController.navigate(uri)
    }

    fun calculateAge(birthdate: String): Int {
        Log.i("CommonFun", "calculateAge: $birthdate")
        return try {
            val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
            val birthDate = LocalDate.parse(birthdate, formatter)
            val today = LocalDate.now()
            Period.between(birthDate, today).years
        } catch (e: Exception) {
            Log.e("CommonFun", "calculateAge: ${e.message}")
            0
        }
    }

    fun EditText.afterTextChanged(onChanged: () -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onChanged()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
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

    fun animateOnClick(view: View) {
        view.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
        }.start()
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
    fun formatTime(hour: Int, minute: Int): String {
        val formattedHour = if (hour > 12) hour - 12 else hour
        val amPm = if (hour >= 12) "PM" else "AM"
        return String.format("%02d:%02d %s", formattedHour, minute, amPm)
    }

    suspend inline fun <reified T> getGoalById(
        goalId: String,
    ): T? {
        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("goals")
                .document(getCurrentUserId()!!)
                .collection("Habit")
                .document(goalId)
                .get()
                .await()

            snapshot.toObject(T::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getUser(): User? {
        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(getCurrentUserId()!!)
                .get()
                .await()

            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            Log.i("CommonFun", "getUser: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    fun getScreenSize(context: Context): Point {
        val wm = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
        val metrics = wm.maximumWindowMetrics

        return Point(metrics.bounds.width(), metrics.bounds.height())

    }
}