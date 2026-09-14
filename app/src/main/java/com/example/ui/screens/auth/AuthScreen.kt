package com.example.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.util.AppStrings
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Extension helper to safely await any Google Play / Firebase Task in Coroutines
private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(
                task.exception ?: Exception("Task failed with unknown error")
            )
        }
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: (userId: String, email: String, name: String) -> Unit,
    onContinueAsGuest: () -> Unit,
    lang: String = "en"
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }
    var showGoogleNotConfiguredDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = remember {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    // Check if there is an active Firebase user waiting for email verification
    var pendingVerificationUser by remember {
        mutableStateOf<FirebaseUser?>(auth?.currentUser?.takeIf { !it.isEmailVerified })
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .testTag("auth_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo & Header
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = AppStrings.get("app_name", lang),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (pendingVerificationUser != null) {
                    "Email Verification Required"
                } else if (isSignUp) {
                    AppStrings.get("auth_register_title", lang)
                } else {
                    AppStrings.get("auth_login_title", lang)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Email Verification Required Card
        if (pendingVerificationUser != null) {
            val user = pendingVerificationUser!!
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_verification_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MarkEmailRead,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = "Verify Your Email Address",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "A verification email has been sent to:\n${user.email.orEmpty()}\n\nPlease check your email inbox and spam folder, click the verification link, and then tap below to continue.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        if (infoMessage != null) {
                            Text(
                                text = infoMessage ?: "",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                textAlign = TextAlign.Center
                            )
                        }

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Button: I verified my email
                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                infoMessage = null
                                coroutineScope.launch {
                                    try {
                                        user.reload().awaitTask()
                                        if (user.isEmailVerified) {
                                            isLoading = false
                                            onAuthSuccess(
                                                user.uid,
                                                user.email.orEmpty(),
                                                user.displayName.orEmpty().ifBlank { user.email?.substringBefore("@") ?: "Student" }
                                            )
                                        } else {
                                            isLoading = false
                                            errorMessage = "Your email has not been verified yet. Please click the link in your email and try again."
                                        }
                                    } catch (e: Exception) {
                                        isLoading = false
                                        errorMessage = e.localizedMessage ?: "Failed to verify email status."
                                    }
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_check_email_verified")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "I Verified My Email",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Button: Resend verification email
                        OutlinedButton(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                infoMessage = null
                                coroutineScope.launch {
                                    try {
                                        user.sendEmailVerification().awaitTask()
                                        isLoading = false
                                        infoMessage = "Verification email sent again. Check your inbox and spam folder."
                                    } catch (e: Exception) {
                                        isLoading = false
                                        errorMessage = e.localizedMessage ?: "Failed to resend verification email."
                                    }
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_resend_verification_email")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Resend Verification Email")
                        }

                        // Sign Out / Use another account
                        TextButton(
                            onClick = {
                                auth?.signOut()
                                pendingVerificationUser = null
                                errorMessage = null
                                infoMessage = null
                            },
                            modifier = Modifier.testTag("btn_switch_account_verification")
                        ) {
                            Text(
                                text = "Sign in with a different account",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        } else {
            // Form Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Name Field (Sign Up only)
                        AnimatedVisibility(visible = isSignUp) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text(AppStrings.get("label_full_name", lang)) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_auth_name"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                errorMessage = null
                            },
                            label = { Text(AppStrings.get("label_email", lang)) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_auth_email"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = { Text(AppStrings.get("label_password", lang)) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_auth_password"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Forgot Password link (Login only)
                        if (!isSignUp) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = AppStrings.get("btn_forgot_password", lang),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable {
                                            forgotPasswordEmail = email
                                            showForgotPasswordDialog = true
                                        }
                                        .testTag("btn_forgot_password")
                                )
                            }
                        }

                        // Error Message Display
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Primary Action Button (Login / Register)
                        Button(
                            onClick = {
                                val trimmedEmail = email.trim()
                                val trimmedPassword = password.trim()
                                if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
                                    errorMessage = "Please provide both email and password."
                                    return@Button
                                }
                                if (trimmedPassword.length < 6) {
                                    errorMessage = "Password must be at least 6 characters long."
                                    return@Button
                                }
                                if (isSignUp && name.isBlank()) {
                                    errorMessage = "Please enter your name."
                                    return@Button
                                }

                                if (auth == null) {
                                    errorMessage = "Firebase Authentication is unavailable on this device."
                                    return@Button
                                }

                                isLoading = true
                                errorMessage = null
                                infoMessage = null

                                coroutineScope.launch {
                                    try {
                                        if (isSignUp) {
                                            val result = auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPassword).awaitTask()
                                            val user = result.user
                                            if (user != null) {
                                                try {
                                                    val profileUpdates = UserProfileChangeRequest.Builder()
                                                        .setDisplayName(name.trim())
                                                        .build()
                                                    user.updateProfile(profileUpdates).awaitTask()
                                                } catch (pe: Exception) {
                                                    android.util.Log.w("AuthScreen", "Failed to set display name", pe)
                                                }
                                                try {
                                                    user.sendEmailVerification().awaitTask()
                                                } catch (ve: Exception) {
                                                    android.util.Log.w("AuthScreen", "Failed to send initial verification email", ve)
                                                }
                                                isLoading = false
                                                pendingVerificationUser = user
                                            } else {
                                                isLoading = false
                                                errorMessage = "User creation failed: null user received."
                                            }
                                        } else {
                                            val result = auth.signInWithEmailAndPassword(trimmedEmail, trimmedPassword).awaitTask()
                                            val user = result.user
                                            if (user != null) {
                                                user.reload().awaitTask()
                                                isLoading = false
                                                if (user.isEmailVerified) {
                                                    onAuthSuccess(
                                                        user.uid,
                                                        trimmedEmail,
                                                        user.displayName ?: name.ifBlank { trimmedEmail.substringBefore("@") }
                                                    )
                                                } else {
                                                    pendingVerificationUser = user
                                                }
                                            } else {
                                                isLoading = false
                                                errorMessage = "Login failed: user not found."
                                            }
                                        }
                                    } catch (e: Exception) {
                                        isLoading = false
                                        errorMessage = e.localizedMessage ?: "Authentication failed. Please check your credentials."
                                    }
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_auth_primary")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isSignUp) AppStrings.get("btn_register", lang) else AppStrings.get("btn_login", lang),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Google Sign-In Alternative
                        OutlinedButton(
                            onClick = {
                                showGoogleNotConfiguredDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_auth_google")
                        ) {
                            Text("Sign in with Google", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // Switch between Sign In / Sign Up
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSignUp) AppStrings.get("already_have_account", lang) else AppStrings.get("dont_have_account", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSignUp) AppStrings.get("btn_login", lang) else AppStrings.get("btn_register", lang),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable {
                                        isSignUp = !isSignUp
                                        errorMessage = null
                                    }
                                    .testTag("btn_toggle_auth_mode")
                            )
                        }
                    }
                }
            }
        }
    }

    // Google Sign-In Not Configured Dialog
    if (showGoogleNotConfiguredDialog) {
        AlertDialog(
            onDismissRequest = { showGoogleNotConfiguredDialog = false },
            title = { Text("Google Sign-In") },
            text = {
                Text(
                    text = "Google Sign-In is not currently configured for this Firebase project. Please create an account or sign in with your Email and Password using Firebase Authentication.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { showGoogleNotConfiguredDialog = false },
                    modifier = Modifier.testTag("btn_dismiss_google_dialog")
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text(AppStrings.get("btn_forgot_password", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter your email address to receive password reset instructions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = forgotPasswordEmail,
                        onValueChange = { forgotPasswordEmail = it },
                        label = { Text(AppStrings.get("label_email", lang)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_forgot_email")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (forgotPasswordEmail.isNotBlank()) {
                            auth?.sendPasswordResetEmail(forgotPasswordEmail.trim())
                            Toast.makeText(context, "Password reset email dispatched!", Toast.LENGTH_LONG).show()
                            showForgotPasswordDialog = false
                        }
                    },
                    modifier = Modifier.testTag("btn_confirm_forgot_password")
                ) {
                    Text(AppStrings.get("btn_submit", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text(AppStrings.get("btn_cancel", lang))
                }
            }
        )
    }
}
