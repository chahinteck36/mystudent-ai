package com.example.data.ai

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.AppCheckProvider
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.AppCheckToken
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * AppCheckProvider that exchanges a pre-registered debug secret with Firebase App Check.
 * Ensures consistent and reliable authentication with Firebase AI Logic (Gemini API)
 * without generating ephemeral random tokens that fail console allowlist verification.
 */
class RegisteredAppCheckProvider(
    private val firebaseApp: FirebaseApp,
    private val debugSecret: String
) : AppCheckProvider {

    init {
        require(debugSecret.isNotBlank()) {
            "RegisteredAppCheckProvider requires a non-empty debug secret provided via BuildConfig or development environment."
        }
    }

    private val executor = Executors.newSingleThreadExecutor()
    @Volatile
    private var cachedToken: AppCheckToken? = null

    override fun getToken(): Task<AppCheckToken> {
        val current = cachedToken
        if (current != null && current.expireTimeMillis > System.currentTimeMillis() + BUFFER_MILLIS) {
            return Tasks.forResult(current)
        }

        val source = TaskCompletionSource<AppCheckToken>()
        executor.execute {
            try {
                val token = exchangeDebugToken(debugSecret)
                cachedToken = token
                Log.d(TAG, "Successfully exchanged registered App Check debug token")
                source.setResult(token)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to exchange registered App Check debug token", e)
                source.setException(e)
            }
        }
        return source.task
    }

    override fun getLimitedUseToken(): Task<AppCheckToken> = getToken()

    private fun exchangeDebugToken(secret: String): AppCheckToken {
        val options = firebaseApp.options
        val projectId = options.projectId
        val appId = options.applicationId
        val apiKey = options.apiKey

        val urlString = "https://firebaseappcheck.googleapis.com/v1/projects/$projectId/apps/$appId:exchangeDebugToken?key=$apiKey"
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 15000
        conn.readTimeout = 15000

        val requestBody = JSONObject().apply {
            put("debug_token", secret)
        }.toString()

        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(requestBody)
            writer.flush()
        }

        val responseCode = conn.responseCode
        if (responseCode !in 200..299) {
            val errorResponse = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            throw IllegalStateException("App Check token exchange returned HTTP $responseCode: $errorResponse")
        }

        val responseText = conn.inputStream.bufferedReader().use { it.readText() }
        val json = JSONObject(responseText)
        val tokenString = json.getString("token")
        val ttlString = json.optString("ttl", "3600s")
        val ttlSeconds = ttlString.replace("s", "").trim().toLongOrNull() ?: 3600L
        val expireTimeMillis = System.currentTimeMillis() + (ttlSeconds * 1000L)

        return object : AppCheckToken() {
            override fun getToken(): String = tokenString
            override fun getExpireTimeMillis(): Long = expireTimeMillis
        }
    }

    companion object {
        private const val TAG = "RegisteredAppCheck"
        private const val BUFFER_MILLIS = 300_000L // 5 minutes buffer
    }
}

class RegisteredAppCheckProviderFactory(
    private val debugSecret: String
) : AppCheckProviderFactory {
    override fun create(firebaseApp: FirebaseApp): AppCheckProvider {
        return RegisteredAppCheckProvider(firebaseApp, debugSecret)
    }
}
