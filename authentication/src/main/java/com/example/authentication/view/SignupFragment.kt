package com.example.authentication.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.example.authentication.compose.SignUpScreen
import com.example.authentication.google.GoogleAuthHelper
import com.example.authentication.google.handleGoogleResult
import com.example.designsystem.theme.PactTheme
import com.example.utils.CommonFun
import com.example.utils.Prefs
import com.example.utils.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var isLoading by mutableStateOf(false)
    // null = unknown/empty, true = available, false = taken
    private var usernameAvailable by mutableStateOf<Boolean?>(null)

    private val handler = Handler(Looper.getMainLooper())
    private var usernameCheck: Runnable? = null

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
                    SignUpScreen(
                        modifier = Modifier.systemBarsPadding(),
                        isLoading = isLoading,
                        usernameAvailable = usernameAvailable,
                        onUsernameChange = ::onUsernameChanged,
                        onSignUp = { name, username, email, password ->
                            if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                                Toast.makeText(requireContext(), "All fields are required!", Toast.LENGTH_SHORT).show()
                            } else {
                                signUpUser(name, username, email, password)
                            }
                        },
                        onSignIn = { findNavController().navigate(R.id.loginFragment) },
                        onBack = { findNavController().navigateUp() },
                        onSocial = { provider ->
                            if (provider == "Google") startGoogleSignIn()
                            else Toast.makeText(requireContext(), "$provider sign-up coming soon", Toast.LENGTH_SHORT).show()
                        },
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        usernameCheck?.let { handler.removeCallbacks(it) }
    }

    /** Debounced availability check — mirrors the previous TextWatcher behavior. */
    private fun onUsernameChanged(username: String) {
        usernameCheck?.let { handler.removeCallbacks(it) }
        if (username.isEmpty()) {
            usernameAvailable = null
            return
        }
        usernameCheck = Runnable { checkUsernameAvailability(username) }
        handler.postDelayed(usernameCheck!!, 500)
    }

    private fun checkUsernameAvailability(username: String) {
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents -> usernameAvailable = documents.isEmpty }
            .addOnFailureListener { e -> Log.e("Firestore", "Error checking username", e) }
    }

    private fun signUpUser(name: String, username: String, email: String, password: String) {
        isLoading = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // Onboarding (AI user-profiling) is skipped for now — new users go straight to Home.
                    val userData = User(id = user?.uid!!, name = name, username = username, email = email, onboardingCompleted = true)
                    user.sendEmailVerification()
                        .addOnSuccessListener {
                            saveUserData(userData)
                            CommonFun.deepLinkNav("homeFragment", requireContext())
                        }
                        .addOnFailureListener {
                            isLoading = false
                            Toast.makeText(requireContext(), "Failed to send verification email!", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    isLoading = false
                    Toast.makeText(requireContext(), "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserData(user: User) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener {
                    isLoading = false
                    Prefs.setOnboardingCompleted(requireContext(), true)
                }
                .addOnFailureListener { exception ->
                    isLoading = false
                    Toast.makeText(requireContext(), "Failed to save user data: ${exception.message}", Toast.LENGTH_LONG).show()
                    Log.e("FirestoreError", "Error saving user data", exception)
                }
        }
    }
}
