package com.example.ui.screens.history

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.LectureNoteEntity
import com.example.data.local.entity.LessonSummaryEntity
import com.example.data.local.entity.ProblemSolutionEntity
import com.example.data.local.entity.QuizEntity
import com.example.ui.util.AppStrings

sealed class HistoryItem(
    val id: Long,
    val title: String,
    val category: String,
    val dateLabel: String,
    val type: String
) {
    class Problem(val entity: ProblemSolutionEntity) : HistoryItem(
        id = entity.id,
        title = entity.problemText,
        category = entity.subject,
        dateLabel = "Solved with AI",
        type = "Problem"
    )

    class Summary(val entity: LessonSummaryEntity) : HistoryItem(
        id = entity.id,
        title = entity.title,
        category = entity.subject,
        dateLabel = "Lesson Summary",
        type = "Summary"
    )

    class Quiz(val entity: QuizEntity) : HistoryItem(
        id = entity.id,
        title = entity.title,
        category = entity.subject,
        dateLabel = "Score: ${entity.scorePercentage}%",
        type = "Quiz"
    )

    class Lecture(val entity: LectureNoteEntity) : HistoryItem(
        id = entity.id,
        title = entity.title,
        category = entity.subject,
        dateLabel = "Lecture Recording",
        type = "Lecture"
    )
}

@Composable
fun HistoryScreen(
    solutions: List<ProblemSolutionEntity>,
    summaries: List<LessonSummaryEntity>,
    quizzes: List<QuizEntity>,
    lectures: List<LectureNoteEntity>,
    onSelectSolution: (ProblemSolutionEntity) -> Unit,
    onSelectSummary: (LessonSummaryEntity) -> Unit,
    onSelectQuiz: (QuizEntity) -> Unit,
    onDeleteSolution: (Long) -> Unit,
    onDeleteSummary: (Long) -> Unit,
    onDeleteQuiz: (Long) -> Unit,
    lang: String = "en"
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val allItems = remember(solutions, summaries, quizzes, lectures) {
        val list = mutableListOf<HistoryItem>()
        solutions.forEach { list.add(HistoryItem.Problem(it)) }
        summaries.forEach { list.add(HistoryItem.Summary(it)) }
        quizzes.forEach { list.add(HistoryItem.Quiz(it)) }
        lectures.forEach { list.add(HistoryItem.Lecture(it)) }
        list
    }

    val filteredList = allItems.filter { item ->
        val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Problems" -> item is HistoryItem.Problem
            "Summaries" -> item is HistoryItem.Summary
            "Quizzes" -> item is HistoryItem.Quiz
            "Lectures" -> item is HistoryItem.Lecture
            else -> true
        }
        matchesSearch && matchesFilter
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("history_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = AppStrings.get("nav_history", lang),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Complete chronological log of all your academic inquiries",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Search
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search past questions, topics, summaries...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("input_search_history")
            )
        }

        // Filters
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Problems", "Summaries", "Quizzes", "Lectures").forEach { filter ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedFilter == filter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { selectedFilter = filter }
                    ) {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (selectedFilter == filter) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        if (filteredList.isEmpty()) {
            item {
                Text(
                    text = "No history entries found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        } else {
            items(filteredList) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            when (item) {
                                is HistoryItem.Problem -> onSelectSolution(item.entity)
                                is HistoryItem.Summary -> onSelectSummary(item.entity)
                                is HistoryItem.Quiz -> onSelectQuiz(item.entity)
                                is HistoryItem.Lecture -> {}
                            }
                        }
                        .testTag("history_item_${item.type}_${item.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (icon, color) = when (item) {
                            is HistoryItem.Problem -> Icons.Default.CameraAlt to MaterialTheme.colorScheme.primary
                            is HistoryItem.Summary -> Icons.Default.Subject to MaterialTheme.colorScheme.secondary
                            is HistoryItem.Quiz -> Icons.Default.Quiz to Color(0xFF10B981)
                            is HistoryItem.Lecture -> Icons.Default.Mic to Color(0xFF7C3AED)
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = "${item.category} • ${item.dateLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                when (item) {
                                    is HistoryItem.Problem -> onDeleteSolution(item.id)
                                    is HistoryItem.Summary -> onDeleteSummary(item.id)
                                    is HistoryItem.Quiz -> onDeleteQuiz(item.id)
                                    is HistoryItem.Lecture -> {}
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
