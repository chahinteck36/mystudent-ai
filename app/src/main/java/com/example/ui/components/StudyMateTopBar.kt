package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserProfileEntity
import com.example.ui.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyMateTopBar(
    currentRoute: String,
    userProfile: UserProfileEntity?,
    onOpenDrawer: () -> Unit,
    onLanguageToggle: () -> Unit,
    onThemeToggle: () -> Unit,
    onAdminClick: () -> Unit
) {
    val lang = userProfile?.selectedLanguage ?: "en"

    val title = when (currentRoute) {
        "landing" -> AppStrings.get("app_name", lang)
        "auth" -> "Authentication"
        "onboarding" -> "Set Up Study Plan"
        "dashboard" -> AppStrings.get("app_name", lang)
        "solver" -> AppStrings.get("solver_title", lang)
        "tutor" -> AppStrings.get("tutor_title", lang)
        "summary" -> AppStrings.get("summary_title", lang)
        "flashcards" -> AppStrings.get("flashcards_title", lang)
        "quiz" -> AppStrings.get("quiz_title", lang)
        "lectures" -> AppStrings.get("lecture_title", lang)
        "library" -> AppStrings.get("nav_library", lang)
        "history" -> AppStrings.get("nav_history", lang)
        "progress" -> AppStrings.get("nav_progress", lang)
        "settings" -> AppStrings.get("settings_title", lang)
        "admin" -> "Admin Console"
        else -> AppStrings.get("app_name", lang)
    }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        },
        navigationIcon = {
            if (currentRoute != "landing" && currentRoute != "auth") {
                IconButton(onClick = onOpenDrawer, modifier = Modifier.testTag("btn_open_drawer")) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Navigation Menu",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        actions = {
            // Plan Badge
            if (userProfile != null && currentRoute != "landing" && currentRoute != "auth") {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (userProfile.plan == "Pro") Color(0xFF10B981) else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = userProfile.plan,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (userProfile.plan == "Pro") Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Language Toggle
            IconButton(onClick = onLanguageToggle, modifier = Modifier.size(36.dp).testTag("topbar_btn_lang")) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = "Switch Language",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Dark Mode Toggle
            IconButton(onClick = onThemeToggle, modifier = Modifier.size(36.dp).testTag("topbar_btn_theme")) {
                Icon(
                    imageVector = if (userProfile?.isDarkMode == true) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Theme",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Admin Shortcut
            IconButton(onClick = onAdminClick, modifier = Modifier.size(36.dp).testTag("topbar_btn_admin")) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.testTag("app_top_bar")
    )
}
