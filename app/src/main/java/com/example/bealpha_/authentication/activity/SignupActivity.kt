package com.example.bealpha_.authentication.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bealpha_.MainActivity
import com.example.bealpha_.R
import com.example.bealpha_.authentication.model.User
import com.example.bealpha_.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
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

        textWatcher(binding.etUsername)


        passwordVisibility(binding.showPassword, binding.etPassword)

        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signUpUser(name, username, email, password)
        }
    }


    private fun signUpUser(name: String, username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val user = User(id = userId, name = name, username = username, email = email)
                        db.collection("users").document(userId)
                            .set(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to save user data!", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun textWatcher(editText: EditText)
    {
        editText.addTextChangedListener(object : TextWatcher {
            private var handler = Handler(Looper.getMainLooper()) // For debouncing
            private var workRunnable: Runnable? = null

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val username = s.toString().trim()

                // Cancel previous checks to prevent excessive Firestore calls
                workRunnable?.let { handler.removeCallbacks(it) }

                if (username.isNotEmpty()) {
                    workRunnable = Runnable { checkUsernameAvailability(username) }
                    handler.postDelayed(workRunnable!!, 500) // Debounce: wait 500ms before checking
                }
                else{
                    binding.ivUsernameStatus.visibility = View.GONE
                }

            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun checkUsernameAvailability(username: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    binding.ivUsernameStatus.setImageResource(R.drawable.check_green)
                } else {
                    binding.ivUsernameStatus.setImageResource(R.drawable.cross_red)
                }
                binding.ivUsernameStatus.visibility = View.VISIBLE
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error checking username", e)
            }
    }

    private fun passwordVisibility(ivEye:ImageView, etPassword:EditText)
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