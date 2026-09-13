package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.util.AppStrings

@Composable
fun StudyMateBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    lang: String = "en"
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text(AppStrings.get("nav_home", lang)) },
            selected = currentRoute == "dashboard" || currentRoute == "landing",
            onClick = { onNavigate("dashboard") },
            modifier = Modifier.testTag("nav_item_home"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Solve") },
            label = { Text(AppStrings.get("nav_solve", lang)) },
            selected = currentRoute == "solver",
            onClick = { onNavigate("solver") },
            modifier = Modifier.testTag("nav_item_solve"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Chat, contentDescription = "Tutor") },
            label = { Text(AppStrings.get("nav_tutor", lang)) },
            selected = currentRoute == "tutor",
            onClick = { onNavigate("tutor") },
            modifier = Modifier.testTag("nav_item_tutor"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Psychology, contentDescription = "Cards") },
            label = { Text(AppStrings.get("nav_flashcards", lang)) },
            selected = currentRoute == "flashcards" || currentRoute == "quizzes",
            onClick = { onNavigate("flashcards") },
            modifier = Modifier.testTag("nav_item_flashcards"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Settings") },
            label = { Text(AppStrings.get("nav_settings", lang)) },
            selected = currentRoute == "settings" || currentRoute == "library",
            onClick = { onNavigate("settings") },
            modifier = Modifier.testTag("nav_item_settings"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}
