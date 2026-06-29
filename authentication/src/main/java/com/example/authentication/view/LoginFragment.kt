package com.example.authentication.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.authentication.R
import com.example.authentication.compose.SignInScreen
import com.example.authentication.google.GoogleAuthHelper
import com.example.authentication.google.handleGoogleResult
import com.example.designsystem.theme.PactTheme
import com.example.utils.CommonFun
import com.example.utils.Prefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var isLoading by mutableStateOf(false)

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleGoogleResult(
                data = result.data,
                onExisting = {
                    isLoading = false
                    Prefs.setOnboardingCompleted(requireContext(), true)
                    CommonFun.deepLinkNav("homeFragment", requireContext())
                },
                onNewUser = {
                    isLoading = false
                    findNavController().navigate(R.id.completeProfileFragment)
                },
                onError = {
                    isLoading = false
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                },
                onCancel = { isLoading = false },
            )
        }

    private fun startGoogleSignIn() {
        val client = GoogleAuthHelper.signInClient(requireContext())
        if (client == null) {
            Toast.makeText(requireContext(), "Google sign-in isn't configured yet", Toast.LENGTH_SHORT).show()
            return
        }
        isLoading = true
        client.signOut().addOnCompleteListener {
            googleSignInLauncher.launch(client.signInIntent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PactTheme {
                    SignInScreen(
                        modifier = Modifier.systemBarsPadding(),
                        isLoading = isLoading,
                        onSignIn = { username, password ->
                            if (username.isNotEmpty() && password.isNotEmpty()) {
                                loginUser(username, password)
                            } else {
                                Toast.makeText(requireContext(), "Please enter both username and password.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onForgot = { findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment) },
                        onBack = { findNavController().navigateUp() },
                        onCreateAccount = { findNavController().navigate(R.id.signupFragment) },
                        onSocial = { provider ->
                            if (provider == "Google") startGoogleSignIn()
                            else Toast.makeText(requireContext(), "$provider sign-in coming soon", Toast.LENGTH_SHORT).show()
                        },
                    )
                }
            }
        }
    }

    private fun loginUser(username: String, password: String) {
        isLoading = true
        Log.i("LoginFragment", "loginUser: $username")
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val email = result.documents.firstOrNull()?.getString("email")
                    if (email != null) {
                        authenticateWithEmailAndPassword(email, password)
                    } else {
                        isLoading = false
                        Toast.makeText(requireContext(), "Username does not exist.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    isLoading = false
                    Toast.makeText(requireContext(), "Username not found in the database.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                isLoading = false
                Toast.makeText(requireContext(), "Failed to fetch username from Firestore.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun authenticateWithEmailAndPassword(email: String, password: String) {
        Log.i("LoginFragment", "authenticateWithEmailAndPassword: $email")
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        navigateAsOnboardingCompleted(user.uid)
                    }
                } else {
                    isLoading = false
                    Toast.makeText(requireContext(), "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateAsOnboardingCompleted(userId: String) {
        Log.i("LoginFragment", ".navigateAsOnboardingCompleted: $userId")
        // Onboarding (AI user-profiling) is skipped for now — every signed-in user goes to Home.
        isLoading = false
        Prefs.setOnboardingCompleted(requireContext(), true)
        CommonFun.deepLinkNav("homeFragment", requireContext())
    }
}
