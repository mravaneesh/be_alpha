package com.example.bealpha_.authentication.activity

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bealpha_.MainActivity
import com.example.bealpha_.R
import com.example.bealpha_.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.ivBackArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()  // This will take you back to the previous activity
        }

        passwordVisibility(binding.showPassword, binding.etPassword)

        // Set up the Login button click listener
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                loginUser(username, password)
            } else {
                Toast.makeText(this, "Please enter both username and password.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(username: String, password: String) {
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val email = result.documents.firstOrNull()?.getString("email")

                    if (email != null) {
                        // If email found, authenticate the user with email and password
                        authenticateWithEmailAndPassword(email, password)
                    } else {
                        Toast.makeText(this, "Username does not exist.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Username not found in the database.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to fetch username from Firestore.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun authenticateWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // If login is successful, move to the next screen
                    val user = auth.currentUser
                    if (user != null) {
                        // Proceed to HostActivity
                        startActivity(Intent(this, MainActivity::class.java))
                        finish() // Close the login activity
                    }
                } else {
                    // If sign-in fails, show an error message
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun passwordVisibility(ivEye: ImageView, etPassword: EditText)
    {
        // Flag to check whether password is visible or hidden
        var isPasswordVisible = false
        ivEye.setOnClickListener {
            if (isPasswordVisible) {
                // Hide password
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivEye.setImageResource(R.drawable.show_password)
            } else {
                // Show password
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivEye.setImageResource(R.drawable.hide_password)
            }

            // Move cursor to the end of the text after inputType change
            etPassword.setSelection(etPassword.text.length)

            // Toggle the flag
            isPasswordVisible = !isPasswordVisible
        }
    }
}