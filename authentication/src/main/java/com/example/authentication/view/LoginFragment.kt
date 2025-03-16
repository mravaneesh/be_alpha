package com.example.authentication.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.utils.R
import com.example.authentication.databinding.FragmentLoginBinding
import com.example.utils.CommonFun
import com.example.utils.CommonFun.applyScaleAnimation
import com.example.utils.CommonFun.passwordVisibility
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? =null
    private val binding: FragmentLoginBinding
        get() = _binding!!
    private lateinit var db:FirebaseFirestore
    private lateinit var auth:FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initView()
        return binding.root
    }

    private fun initView() {
        passwordVisibility(binding.showPassword, binding.etPassword)
        binding.btnLogin.applyScaleAnimation()
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                loginUser(username, password)
            } else {
                Toast.makeText(requireContext(), "Please enter both username and password.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(com.example.authentication.R.id.action_loginFragment_to_forgotPasswordFragment)
        }
    }

    private fun loginUser(username: String, password: String) {
        setLoadingState()
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val email = result.documents.firstOrNull()?.getString("email")
                    if (email != null) {
                        authenticateWithEmailAndPassword(email, password)
                    } else {
                        setNormalState()
                        Toast.makeText(requireContext(), "Username does not exist.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    setNormalState()
                    Toast.makeText(requireContext(), "Username not found in the database.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                setNormalState()
                Toast.makeText(requireContext(), "Failed to fetch username from Firestore.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun authenticateWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                setNormalState()
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        CommonFun.deepLinkNav("homeFragment",requireContext())
                    }
                } else {
                    Toast.makeText(requireContext(), "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setLoadingState() {
        binding.btnLogin.visibility = View.GONE  // Hide button text
        binding.lottieProgress.visibility = View.VISIBLE // Show Lottie animation
        binding.tvForgotPassword.visibility = View.GONE
        binding.etUsername.setTextColor(ContextCompat.getColor(requireContext(), R.color.cool_gray))  // Change text color
        binding.etPassword.setTextColor(ContextCompat.getColor(requireContext(), R.color.cool_gray))
    }
    private fun setNormalState() {
        binding.btnLogin.visibility = View.VISIBLE  // Show button text
        binding.lottieProgress.visibility = View.GONE // Hide Lottie animation
        binding.tvForgotPassword.visibility = View.VISIBLE
        binding.etUsername.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))  // Reset text color
        binding.etPassword.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        binding.etPassword.text?.clear()
    }
}