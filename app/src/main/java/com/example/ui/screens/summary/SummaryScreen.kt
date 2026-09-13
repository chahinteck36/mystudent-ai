package com.example.ui.screens.summary

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.LessonSummaryEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.ui.util.AppStrings

@Composable
fun SummaryScreen(
    currentSummary: LessonSummaryEntity?,
    isSummarizing: Boolean,
    userProfile: UserProfileEntity?,
    onGenerateSummary: (title: String, subject: String, text: String, length: String) -> Unit,
    onConvertToCards: (title: String, content: String) -> Unit,
    onConvertToQuiz: (subject: String, topic: String) -> Unit,
    lang: String = "en"
) {
    var titleInput by remember { mutableStateOf("") }
    var textInput by remember { mutableStateOf("") }
    var selectedLength by remember { mutableStateOf("standard") }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("summary_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = AppStrings.get("summary_title", lang),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Transform lengthy course notes or articles into structured study guides",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Input Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("summary_input_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        modifier = Modifier.fillMaxWidth().testTag("input_summary_title"),
                        placeholder = { Text("Lesson / Document Title (e.g., Photosynthesis & Cellular Respiration)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("input_summary_text"),
                        placeholder = { Text(AppStrings.get("summary_input_hint", lang)) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Length Toggle
                    Text(
                        text = "Summary Depth:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("short", "standard", "detailed").forEach { len ->
                            val label = when (len) {
                                "short" -> AppStrings.get("length_short", lang)
                                "standard" -> AppStrings.get("length_standard", lang)
                                else -> AppStrings.get("length_detailed", lang)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedLength == len) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedLength = len }
                                    .testTag("chip_length_$len")
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selectedLength == len) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (selectedLength == len) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val subject = userProfile?.major ?: "Academic"
                            onGenerateSummary(titleInput, subject, textInput, selectedLength)
                        },
                        enabled = !isSummarizing && textInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_generate_summary"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isSummarizing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Synthesizing Smart Summary...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(AppStrings.get("btn_summarize", lang))
                        }
                    }
                }
            }
        }

        // Summary Results (All 7 Sections)
        if (currentSummary != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = currentSummary.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${currentSummary.subject} • ${currentSummary.lengthType.replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                val md = """
                                    # ${currentSummary.title}
                                    ## Overview
                                    ${currentSummary.overview}
                                    ## Key Concepts
                                    ${currentSummary.keyConcepts}
                                    ## Definitions
                                    ${currentSummary.definitions}
                                    ## Formulas
                                    ${currentSummary.formulas}
                                """.trimIndent()
                                clipboardManager.setText(AnnotatedString(md))
                                Toast.makeText(context, "Summary copied as Markdown", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }
                }
            }

            // Quick Actions: Convert to Cards or Quiz
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val content = "${currentSummary.overview}\n${currentSummary.keyConcepts}\n${currentSummary.definitions}"
                            onConvertToCards(currentSummary.title, content)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("btn_convert_summary_to_cards")
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Flashcards")
                    }

                    Button(
                        onClick = {
                            onConvertToQuiz(currentSummary.subject, currentSummary.title)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("btn_convert_summary_to_quiz")
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generate Quiz")
                    }
                }
            }

            // 1. Overview
            item {
                SummarySectionCard(
                    title = AppStrings.get("overview", lang),
                    content = currentSummary.overview,
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }

            // 2. Key Concepts
            item {
                SummarySectionCard(
                    title = AppStrings.get("key_concepts", lang),
                    content = currentSummary.keyConcepts,
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }

            // 3. Important Definitions
            item {
                SummarySectionCard(
                    title = AppStrings.get("definitions", lang),
                    content = currentSummary.definitions,
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }

            // 4. Formulas & Rules
            if (currentSummary.formulas.isNotBlank()) {
                item {
                    SummarySectionCard(
                        title = AppStrings.get("formulas", lang),
                        content = currentSummary.formulas,
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                }
            }

            // 5. Examples
            if (currentSummary.examples.isNotBlank()) {
                item {
                    SummarySectionCard(
                        title = AppStrings.get("examples", lang),
                        content = currentSummary.examples,
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                }
            }

            // 6. Things to Remember
            if (currentSummary.thingsToRemember.isNotBlank()) {
                item {
                    SummarySectionCard(
                        title = AppStrings.get("things_to_remember", lang),
                        content = currentSummary.thingsToRemember,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    )
                }
            }

            // 7. Quick Review Checklist
            if (currentSummary.quickReview.isNotBlank()) {
                item {
                    SummarySectionCard(
                        title = AppStrings.get("quick_review", lang),
                        content = currentSummary.quickReview,
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun SummarySectionCard(
    title: String,
    content: String,
    containerColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
