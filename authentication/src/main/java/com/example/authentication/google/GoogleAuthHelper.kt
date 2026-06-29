package com.example.authentication.google

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.example.utils.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Google Sign-In wiring shared across the auth screens. Exchanges a Google ID token for a Firebase
 * session, but does NOT auto-create the profile: brand-new users are routed to the
 * complete-profile screen so they can pick a username. The Web client id comes from the
 * `default_web_client_id` string the google-services plugin generates into :app (resolved by name
 * at runtime since the plugin isn't applied in this module).
 */
object GoogleAuthHelper {

    /** Null when Google sign-in isn't configured yet (no Web client id in google-services.json). */
    fun signInClient(context: Context): GoogleSignInClient? {
        val webClientId = webClientId(context) ?: return null
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, options)
    }

    private fun webClientId(context: Context): String? {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        return if (resId != 0) context.getString(resId) else null
    }

    /**
     * Authenticates with Firebase, then reports whether this account already has a profile. For a
     * brand-new account it first writes a DEFAULT profile (name + unique username derived from the
     * Google account) so the user is never left in a broken, profile-less state if they close the
     * app on the customize screen. New users are still routed on to CompleteProfile to refine it.
     */
    fun firebaseSignIn(
        idToken: String,
        onExisting: () -> Unit,
        onNewUser: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val auth = FirebaseAuth.getInstance()
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
            .addOnSuccessListener {
                val user = auth.currentUser ?: return@addOnSuccessListener onError("Google sign-in failed")
                FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                    .addOnSuccessListener { snap ->
                        if (snap.exists()) {
                            onExisting()
                        } else {
                            createUserDoc(
                                uid = user.uid,
                                name = defaultName(user.displayName, user.email),
                                username = defaultUsername(user.email, user.uid),
                                email = user.email.orEmpty(),
                                onSuccess = onNewUser,
                                onError = onError,
                            )
                        }
                    }
                    .addOnFailureListener { onError(it.message ?: "Failed to load profile") }
            }
            .addOnFailureListener { onError(it.message ?: "Google sign-in failed") }
    }

    /** A friendly default display name from the Google account. */
    fun defaultName(displayName: String?, email: String?): String =
        displayName?.takeIf { it.isNotBlank() }
            ?: email?.substringBefore("@")?.takeIf { it.isNotBlank() }
            ?: "Apogee User"

    /** A unique-by-construction default username: sanitized email local-part + a uid suffix. */
    fun defaultUsername(email: String?, uid: String): String {
        val base = email?.substringBefore("@")?.lowercase()
            ?.replace(Regex("[^a-z0-9_]"), "")
            ?.take(15)
            ?.takeIf { it.isNotBlank() }
            ?: "user"
        return "${base}_${uid.takeLast(4).lowercase()}"
    }

    /** Creates the users/{uid} profile after the new user picks a username. */
    fun createUserDoc(
        uid: String,
        name: String,
        username: String,
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val user = User(id = uid, name = name, username = username, email = email, onboardingCompleted = true)
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Failed to save profile") }
    }
}

private const val SIGN_IN_CANCELLED = 12501 // GoogleSignInStatusCodes.SIGN_IN_CANCELLED

/**
 * Unpacks the Google chooser result and continues the Firebase exchange. Top-level so every auth
 * fragment shares one implementation; each only supplies its navigation callbacks.
 */
fun Fragment.handleGoogleResult(
    data: Intent?,
    onExisting: () -> Unit,
    onNewUser: () -> Unit,
    onError: (String) -> Unit,
    onCancel: () -> Unit,
) {
    try {
        val account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
        val idToken = account.idToken
        if (idToken == null) {
            onError("Google sign-in failed: no token")
            return
        }
        GoogleAuthHelper.firebaseSignIn(idToken, onExisting, onNewUser, onError)
    } catch (e: ApiException) {
        if (e.statusCode == SIGN_IN_CANCELLED) onCancel() else onError("Google sign-in failed (${e.statusCode})")
    }
}
