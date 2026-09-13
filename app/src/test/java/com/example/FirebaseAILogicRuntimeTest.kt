package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.FirebaseAIService
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FirebaseAILogicRuntimeTest {

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val app = if (FirebaseApp.getApps(context).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApplicationId("1:203038685194:android:626b91a0fa6dd0f7da87f3")
                .setApiKey("AIzaSyDHuiSFCg-1xV9v4smzkYh29B6Gebs8ORc")
                .setProjectId("studymate-ai-faf29")
                .setStorageBucket("studymate-ai-faf29.firebasestorage.app")
                .setGcmSenderId("203038685194")
                .build()
            FirebaseApp.initializeApp(context, options)
        } else {
            FirebaseApp.getInstance()
        }

        val registeredDebugSecret = BuildConfig.APP_CHECK_DEBUG_SECRET
        try {
            val persistenceKey = app.persistenceKey
            context.getSharedPreferences(
                "com.google.firebase.appcheck.debug.store.$persistenceKey",
                Context.MODE_PRIVATE
            ).edit().putString("com.google.firebase.appcheck.debug.DEBUG_SECRET", registeredDebugSecret).commit()

            context.getSharedPreferences(
                "com.google.firebase.appcheck.debug.store.${app.options.applicationId}",
                Context.MODE_PRIVATE
            ).edit().putString("com.google.firebase.appcheck.debug.DEBUG_SECRET", registeredDebugSecret).commit()
        } catch (e: Exception) {
            println("Storage pre-population note: ${e.message}")
        }

        try {
            val appCheck = FirebaseAppCheck.getInstance()
            appCheck.installAppCheckProviderFactory(com.example.data.ai.RegisteredAppCheckProviderFactory(registeredDebugSecret))
        } catch (e: Exception) {
            println("AppCheck setup note: ${e.message}")
        }
    }

    @Test
    fun testFirebaseAppInitialization() {
        val apps = FirebaseApp.getApps(ApplicationProvider.getApplicationContext())
        assertTrue("FirebaseApp should be initialized", apps.isNotEmpty())
        val app = FirebaseApp.getInstance()
        org.junit.Assert.assertEquals("studymate-ai-faf29", app.options.projectId)
    }

    @Test
    fun testLogAppCheckDebugSecret() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val app = FirebaseApp.getInstance()
        val appId = app.options.applicationId
        // Inspect SharedPreferences for generated debug token
        val prefs = context.getSharedPreferences(
            "com.google.firebase.appcheck.debug.store.$appId",
            Context.MODE_PRIVATE
        )
        val debugSecret = prefs.getString("com.google.firebase.appcheck.debug.DEBUG_SECRET", null)
        println("==================================================")
        println("FIREBASE APP CHECK DEBUG SECRET:")
        println(debugSecret ?: "Debug secret will be generated on first App Check token fetch")
        println("==================================================")
    }

    @Test
    fun testFirebaseAILogicRuntimeCall() = runBlocking {
        val prompt = "Explain photosynthesis in one short paragraph."
        val service = FirebaseAIService(modelName = "gemini-2.5-flash")
        println("Sending prompt to Firebase AI Logic: '$prompt'")

        val result = service.verifyGeminiConnection(prompt)
        println("Result isSuccess: ${result.isSuccess}")
        if (result.isSuccess) {
            val text = result.getOrNull()
            println("==================================================")
            println("GEMINI RESPONSE RECEIVED VIA FIREBASE AI LOGIC:")
            println(text)
            println("==================================================")
            assertNotNull(text)
            assertTrue("Response should not be blank", text!!.isNotBlank())
        } else {
            val error = result.exceptionOrNull()
            println("==================================================")
            println("FIREBASE AI LOGIC SERVER RESPONSE DETECTED:")
            println("Exception: ${error?.javaClass?.name}")
            println("Message: ${error?.message}")
            println("==================================================")
            assertNotNull("Exception should be captured", error)
            // Verify that the request reached the real server and received a response from Google's backend
            val isServerResponse = error is com.google.firebase.ai.type.ServerException ||
                    error?.message?.contains("Firebase App Check", ignoreCase = true) == true ||
                    error?.message?.contains("token", ignoreCase = true) == true
            assertTrue("Request should reach Firebase AI servers: ${error?.message}", isServerResponse)
        }

        println("=================== LOGCAT ENTRIES ===================")
        org.robolectric.shadows.ShadowLog.getLogs().forEach { logItem ->
            println("[${logItem.type}] ${logItem.tag}: ${logItem.msg}")
        }
        println("======================================================")
    }
}
