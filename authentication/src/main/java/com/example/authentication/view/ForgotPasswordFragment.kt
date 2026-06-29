package com.example.authentication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.authentication.compose.ForgotPasswordScreen
import com.example.designsystem.theme.PactTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ForgotPasswordFragment : Fragment() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var isLoading by mutableStateOf(false)
    private var sentMaskedEmail by mutableStateOf<String?>(null)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PactTheme {
                    ForgotPasswordScreen(
                        modifier = Modifier.systemBarsPadding(),
                        isLoading = isLoading,
                        sentMaskedEmail = sentMaskedEmail,
                        onSubmit = ::fetchUserEmail,
                        onDone = { findNavController().navigateUp() },
                        onBack = { findNavController().navigateUp() },
                    )
                }
            }
        }
    }

    private fun fetchUserEmail(username: String) {
        if (username.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your username", Toast.LENGTH_SHORT).show()
            return
        }
        isLoading = true
        firestore.collection("users").whereEqualTo("username", username).get()
            .addOnSuccessListener { documents ->
                val email = documents.documents.firstOrNull()?.getString("email")
                if (!documents.isEmpty && !email.isNullOrEmpty()) {
                    sendPasswordResetEmail(email)
                } else {
                    isLoading = false
                    Toast.makeText(requireContext(), "Username not found!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                isLoading = false
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                isLoading = false
                sentMaskedEmail = maskEmail(email)
            }
            .addOnFailureListener {
                isLoading = false
                Toast.makeText(requireContext(), "Failed to send reset email: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun maskEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size < 2) return email
        val namePart = parts[0]
        val domainPart = parts[1]
        return if (namePart.length > 4) {
            val visibleChars = 2
            val mask = "*".repeat(namePart.length - 2 * visibleChars)
            "${namePart.take(visibleChars)}$mask${namePart.takeLast(visibleChars)}@$domainPart"
        } else {
            "***@$domainPart"
        }
    }
}
