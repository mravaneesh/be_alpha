package com.example.social.data

/**
 * Push delivery config. Set [ENDPOINT] to your deployed server function (see /social-push) once it's
 * live, e.g. "https://apogee-push.vercel.app/api/push".
 *
 * Left blank, pushes are skipped (in-app nudges / challenge invites still work via Firestore) — so
 * nothing breaks before the server is deployed.
 */
object PushConfig {
    const val ENDPOINT = ""
}
