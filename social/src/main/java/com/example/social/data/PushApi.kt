package com.example.social.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Fire a best-effort push via the free server function (see /social-push). The server verifies the
 * caller's ID token and friendship, then builds + sends the FCM message by [type]. No-op until
 * [PushConfig.ENDPOINT] is set, so nothing breaks before the server is deployed.
 */
object PushApi {

    suspend fun notify(auth: FirebaseAuth, toUid: String, type: String, extra: Map<String, String> = emptyMap()) {
        val endpoint = PushConfig.ENDPOINT
        if (endpoint.isBlank()) return
        val idToken = auth.currentUser?.getIdToken(false)?.await()?.token ?: return
        val body = JSONObject().apply {
            put("toUid", toUid)
            put("type", type)
            extra.forEach { (k, v) -> put(k, v) }
        }.toString()
        withContext(Dispatchers.IO) {
            val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $idToken")
                doOutput = true
                connectTimeout = 8_000
                readTimeout = 8_000
            }
            try {
                conn.outputStream.use { it.write(body.toByteArray()) }
                conn.responseCode
            } finally {
                conn.disconnect()
            }
        }
    }
}
