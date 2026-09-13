package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserProfileEntity
import com.example.ui.util.AppStrings

@Composable
fun StudyMateNavDrawer(
    currentRoute: String,
    userProfile: UserProfileEntity?,
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val lang = userProfile?.selectedLanguage ?: "en"

    ModalDrawerSheet(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight()
            .testTag("nav_drawer_sheet"),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp, horizontal = 12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "StudyMate AI",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = userProfile?.major ?: "Academic Assistant",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Items
            DrawerItem(
                label = AppStrings.get("nav_home", lang),
                icon = Icons.Default.Home,
                selected = currentRoute == "dashboard",
                onClick = { onNavigate("dashboard"); onCloseDrawer() },
                tag = "drawer_home"
            )

            DrawerItem(
                label = AppStrings.get("nav_solve", lang),
                icon = Icons.Default.CameraAlt,
                selected = currentRoute == "solver",
                onClick = { onNavigate("solver"); onCloseDrawer() },
                tag = "drawer_solve"
            )

            DrawerItem(
                label = AppStrings.get("nav_tutor", lang),
                icon = Icons.Default.Chat,
                selected = currentRoute == "tutor",
                onClick = { onNavigate("tutor"); onCloseDrawer() },
                tag = "drawer_tutor"
            )

            DrawerItem(
                label = AppStrings.get("nav_summaries", lang),
                icon = Icons.Default.Subject,
                selected = currentRoute == "summaries",
                onClick = { onNavigate("summaries"); onCloseDrawer() },
                tag = "drawer_summaries"
            )

            DrawerItem(
                label = AppStrings.get("nav_flashcards", lang),
                icon = Icons.Default.Psychology,
                selected = currentRoute == "flashcards",
                onClick = { onNavigate("flashcards"); onCloseDrawer() },
                tag = "drawer_flashcards"
            )

            DrawerItem(
                label = AppStrings.get("nav_quizzes", lang),
                icon = Icons.Default.Quiz,
                selected = currentRoute == "quizzes",
                onClick = { onNavigate("quizzes"); onCloseDrawer() },
                tag = "drawer_quizzes"
            )

            DrawerItem(
                label = AppStrings.get("nav_lectures", lang),
                icon = Icons.Default.Mic,
                selected = currentRoute == "lectures",
                onClick = { onNavigate("lectures"); onCloseDrawer() },
                tag = "drawer_lectures"
            )

            DrawerItem(
                label = AppStrings.get("nav_library", lang),
                icon = Icons.Default.Folder,
                selected = currentRoute == "library",
                onClick = { onNavigate("library"); onCloseDrawer() },
                tag = "drawer_library"
            )

            DrawerItem(
                label = AppStrings.get("nav_progress", lang),
                icon = Icons.Default.Timeline,
                selected = currentRoute == "progress",
                onClick = { onNavigate("progress"); onCloseDrawer() },
                tag = "drawer_progress"
            )

            DrawerItem(
                label = AppStrings.get("nav_history", lang),
                icon = Icons.Default.History,
                selected = currentRoute == "history",
                onClick = { onNavigate("history"); onCloseDrawer() },
                tag = "drawer_history"
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            DrawerItem(
                label = AppStrings.get("nav_settings", lang),
                icon = Icons.Default.Settings,
                selected = currentRoute == "settings",
                onClick = { onNavigate("settings"); onCloseDrawer() },
                tag = "drawer_settings"
            )

            DrawerItem(
                label = AppStrings.get("nav_admin", lang),
                icon = Icons.Default.AdminPanelSettings,
                selected = currentRoute == "admin",
                onClick = { onNavigate("admin"); onCloseDrawer() },
                tag = "drawer_admin"
            )

            DrawerItem(
                label = "Landing / About",
                icon = Icons.Default.AutoAwesome,
                selected = currentRoute == "landing",
                onClick = { onNavigate("landing"); onCloseDrawer() },
                tag = "drawer_landing"
            )
        }
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
            )
        },
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .padding(vertical = 2.dp)
            .testTag(tag)
    )
}
