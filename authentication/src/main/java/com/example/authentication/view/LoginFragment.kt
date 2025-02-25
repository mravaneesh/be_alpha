package com.example.authentication.view

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import com.example.utils.R
import com.example.authentication.databinding.FragmentLoginBinding
import com.example.utils.CommonFun
import com.example.utils.ProgressDialogUtil
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

        // Set up the Login button click listener
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                loginUser(username, password)
            } else {
                Toast.makeText(requireContext(), "Please enter both username and password.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(username: String, password: String) {
        ProgressDialogUtil.showProgressDialog(requireContext())
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { result ->
                ProgressDialogUtil.hideProgressDialog()
                if (!result.isEmpty) {
                    val email = result.documents.firstOrNull()?.getString("email")
                    if (email != null) {
                        authenticateWithEmailAndPassword(email, password)
                    } else {
                        Toast.makeText(requireContext(), "Username does not exist.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Username not found in the database.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                ProgressDialogUtil.hideProgressDialog()
                Toast.makeText(requireContext(), "Failed to fetch username from Firestore.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun authenticateWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                ProgressDialogUtil.hideProgressDialog()
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val navController = findNavController()
                        CommonFun.deepLinkNav("android-app://com.example.bealpha_/homeFragment",navController)
                    }
                } else {
                    // If sign-in fails, show an error message
                    Toast.makeText(requireContext(), "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun passwordVisibility(ivEye: ImageView, etPassword: EditText) {
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
}