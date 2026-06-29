package com.example.profile_ui.view

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.designsystem.theme.PactTheme
import com.example.profile_ui.compose.SettingsScreen
import com.example.utils.CommonFun
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val version = runCatching {
            requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName
        }.getOrNull() ?: "1.0"

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PactTheme {
                    SettingsScreen(
                        modifier = Modifier.systemBarsPadding(),
                        email = auth.currentUser?.email ?: "",
                        appVersion = version,
                        onNotifications = ::openNotificationSettings,
                        onReplayWalkthroughs = {
                            com.example.utils.Prefs.resetAllScreenTours(requireContext())
                            Toast.makeText(requireContext(), "Walkthroughs reset — revisit each tab to see them.", Toast.LENGTH_LONG).show()
                        },
                        onTerms = { CommonFun.navigateToDeepLinkFragment(findNavController(), "terms") },
                        onPrivacy = { CommonFun.navigateToDeepLinkFragment(findNavController(), "privacy") },
                        onShare = ::shareApp,
                        onDeleteAccount = ::deleteAccount,
                        onLogout = ::logout,
                        onBack = { findNavController().navigateUp() },
                    )
                }
            }
        }
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
        runCatching { startActivity(intent) }
    }

    private fun shareApp() {
        val share = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, "I'm building better habits with Apogee — join me!")
        startActivity(Intent.createChooser(share, "Share Apogee"))
    }

    private fun deleteAccount() {
        val user = auth.currentUser ?: return restartToWelcome()
        // Best-effort profile doc removal, then the auth account.
        db.collection("users").document(user.uid).delete()
        user.delete()
            .addOnSuccessListener { restartToWelcome() }
            .addOnFailureListener {
                // Usually a recent-login requirement — sign out so they can re-auth and retry.
                Toast.makeText(requireContext(), "Please sign in again, then delete your account.", Toast.LENGTH_LONG).show()
                auth.signOut()
                restartToWelcome()
            }
    }

    private fun logout() {
        auth.signOut()
        restartToWelcome()
    }

    /** Relaunch the app so HostActivity re-evaluates auth state and lands on the welcome screen. */
    private fun restartToWelcome() {
        val context = requireContext().applicationContext
        context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
        requireActivity().finish()
    }
}
