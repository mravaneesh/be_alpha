package com.example.authentication.view

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.utils.R
import com.example.authentication.databinding.FragmentSignupBinding
import com.example.authentication.model.User
import com.example.utils.CommonFun
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initView()
        return binding.root
    }

    private fun initView() {
        textWatcher(binding.etUsername)
        passwordVisibility(binding.showPassword, binding.etPassword)

        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            signUpUser(name, username, email, password)
        }
    }

    private fun signUpUser(name: String, username: String, email: String, password: String) {
        setLoadingState()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userData = User(id = user?.uid!!, name = name, username = username, email = email)
                    user.sendEmailVerification()
                        .addOnSuccessListener {
                            showVerificationDialog(userData)
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to send verification email!", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    setNormalState()
                    Toast.makeText(requireContext(), "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showVerificationDialog(userData:User) {
        val dialog = VerifyEmailDialog(
            onVerified = { checkEmailVerification(userData) },
            onCancel = {
                setNormalState()
                deleteUnverifiedUser()
            }
        )
        dialog.show(parentFragmentManager, "VerifyEmailDialog")
    }

    private fun deleteUnverifiedUser() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    requireContext(),
                    "Signup canceled. Account deleted.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Failed to delete unverified account.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun checkEmailVerification(userData:User) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.reload()?.addOnSuccessListener {
            if (user.isEmailVerified) {
                saveUserData(userData)
                CommonFun.deepLinkNav("homeFragment",requireContext())
            } else {
                Toast.makeText(requireContext(), "Email not verified yet!", Toast.LENGTH_SHORT).show()
                showVerificationDialog(userData) // Show the dialog again
            }
        }
    }

    private fun saveUserData(user: User) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener {
                    setNormalState()
                    CommonFun.deepLinkNav("homeFragment",requireContext())
                }
                .addOnFailureListener {
                    setNormalState()
                    Toast.makeText(requireContext(), "Failed to save user data!", Toast.LENGTH_SHORT).show()
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

    private fun passwordVisibility(ivEye: ImageView, etPassword:EditText)
    {
        var isPasswordVisible = false
        ivEye.setOnClickListener {
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivEye.setImageResource(R.drawable.show_password)
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivEye.setImageResource(R.drawable.hide_password)
            }
            etPassword.setSelection(etPassword.text.length)
            isPasswordVisible = !isPasswordVisible
        }
    }

    private fun setLoadingState() {
        binding.btnSignup.visibility = View.INVISIBLE  // Hide button text
        binding.lottieProgress.visibility = View.VISIBLE // Show Lottie animation
        binding.etUsername.setTextColor(ContextCompat.getColor(requireContext(), R.color.cool_gray))  // Change text color
        binding.etPassword.setTextColor(ContextCompat.getColor(requireContext(), R.color.cool_gray))
        binding.etName.setTextColor(ContextCompat.getColor(requireContext(), R.color.cool_gray))
        binding.etEmail.setTextColor(ContextCompat.getColor(requireContext(), R.color.cool_gray))
    }
    private fun setNormalState() {
        binding.btnSignup.visibility = View.VISIBLE  // Show button text
        binding.lottieProgress.visibility = View.GONE // Hide Lottie animation
        binding.etUsername.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))  // Reset text color
        binding.etPassword.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        binding.etName.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        binding.etEmail.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    }
}