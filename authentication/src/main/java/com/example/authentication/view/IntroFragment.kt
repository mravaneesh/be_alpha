package com.example.authentication.view

import android.os.Bundle
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
import com.example.authentication.compose.WelcomeScreen
import com.example.authentication.google.GoogleAuthHelper
import com.example.authentication.google.handleGoogleResult
import com.example.designsystem.theme.PactTheme
import com.example.utils.CommonFun
import com.example.utils.Prefs

class IntroFragment : Fragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PactTheme {
                    WelcomeScreen(
                        modifier = Modifier.systemBarsPadding(),
                        isLoading = isLoading,
                        onContinueWithGoogle = ::startGoogleSignIn,
                        onTerms = { CommonFun.navigateToDeepLinkFragment(findNavController(), "terms") },
                        onPrivacy = { CommonFun.navigateToDeepLinkFragment(findNavController(), "privacy") },
                    )
                }
            }
        }
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
}
