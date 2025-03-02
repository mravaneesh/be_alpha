package com.example.authentication.view


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.authentication.R
import com.example.authentication.databinding.FragmentForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var userEmail: String? = null  // Store the fetched email

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater,container,false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnContinue.setOnClickListener { fetchUserEmail() }
    }

    private fun fetchUserEmail() {
        val username = binding.etUsername.text.toString().trim()
        if (username.isEmpty()) {
            binding.etUsername.error = "Please enter your username"
            return
        }

        // Query Firestore for email
        firestore.collection("users").whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDoc = documents.documents[0]
                    val userEmail = userDoc.getString("email")

                    if (!userEmail.isNullOrEmpty()) {
                        sendPasswordResetEmail(userEmail)
                    } else {
                        binding.etUsername.error = "Email not found!"
                    }
                } else {
                    binding.etUsername.error = "Username not found!"
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                view?.let { updateUIForEmailFound(email) } // Ensure view is not null before updating UI
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to send reset email: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun updateUIForEmailFound(email: String) {
        binding.etUsername.visibility = View.GONE
        binding.tvDescEmail.text = "Link sent to: ${maskEmail(email)}"
        binding.tvDescEmail.visibility = View.VISIBLE
        binding.btnContinue.text = "Done"
        binding.btnContinue.setOnClickListener { findNavController().navigateUp() }
    }

    private fun maskEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size < 2) return email  // If email format is invalid, return as is

        val namePart = parts[0] // Get the username part before '@'
        val domainPart = parts[1] // Get the domain part after '@'

        return if (namePart.length > 4) {
            val visibleChars = 2 // Number of characters to keep visible at the start and end
            val maskedLength = namePart.length - (2 * visibleChars) // Length of the masked part
            val mask = "*".repeat(maskedLength) // Generate dynamic mask

            "${namePart.take(visibleChars)}$mask${namePart.takeLast(visibleChars)}@$domainPart"
        } else {
            "***@$domainPart" // If too short, mask everything before '@'
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
