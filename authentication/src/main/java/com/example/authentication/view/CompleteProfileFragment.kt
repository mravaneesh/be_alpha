package com.example.authentication.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.example.authentication.compose.CompleteProfileScreen
import com.example.authentication.google.GoogleAuthHelper
import com.example.designsystem.theme.PactTheme
import com.example.utils.CommonFun
import com.example.utils.Prefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/** New-user profile setup after Google sign-in: prefill name from Google, pick a unique username. */
class CompleteProfileFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var isLoading by mutableStateOf(false)
    private var usernameAvailable by mutableStateOf<Boolean?>(null)

    private val handler = Handler(Looper.getMainLooper())
    private var usernameCheck: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val user = auth.currentUser
        val initialName = GoogleAuthHelper.defaultName(user?.displayName, user?.email)
        val initialUsername = GoogleAuthHelper.defaultUsername(user?.email, user?.uid.orEmpty())
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PactTheme {
                    CompleteProfileScreen(
                        modifier = Modifier.systemBarsPadding(),
                        initialName = initialName,
                        initialUsername = initialUsername,
                        usernameAvailable = usernameAvailable,
                        isLoading = isLoading,
                        onUsernameChange = ::onUsernameChanged,
                        onContinue = ::saveProfile,
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        usernameCheck?.let { handler.removeCallbacks(it) }
    }

    /** Debounced availability check, mirroring the sign-up screen. */
    private fun onUsernameChanged(username: String) {
        usernameCheck?.let { handler.removeCallbacks(it) }
        if (username.isEmpty()) {
            usernameAvailable = null
            return
        }
        usernameCheck = Runnable {
            val myUid = auth.currentUser?.uid
            db.collection("users")
                .whereEqualTo("username", username)
                .get()
                // Available if no OTHER user holds it (the user's own default doc doesn't count).
                .addOnSuccessListener { result -> usernameAvailable = result.documents.none { it.id != myUid } }
                .addOnFailureListener { e -> Log.e("CompleteProfile", "username check failed", e) }
        }
        handler.postDelayed(usernameCheck!!, 500)
    }

    private fun saveProfile(name: String, username: String) {
        val user = auth.currentUser ?: return
        isLoading = true
        GoogleAuthHelper.createUserDoc(
            uid = user.uid,
            name = name,
            username = username,
            email = user.email.orEmpty(),
            onSuccess = {
                isLoading = false
                Prefs.setOnboardingCompleted(requireContext(), true)
                CommonFun.deepLinkNav("homeFragment", requireContext())
            },
            onError = {
                isLoading = false
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            },
        )
    }
}
