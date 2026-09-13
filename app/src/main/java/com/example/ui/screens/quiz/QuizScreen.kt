package com.example.ui.screens.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.QuizQuestionResult
import com.example.data.local.entity.QuizEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.ui.util.AppStrings
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun QuizScreen(
    quizzes: List<QuizEntity>,
    activeQuiz: QuizEntity?,
    isGeneratingQuiz: Boolean,
    userProfile: UserProfileEntity?,
    onSelectQuiz: (QuizEntity?) -> Unit,
    onGenerateQuiz: (subject: String, topic: String, count: Int, difficulty: String, type: String) -> Unit,
    onSubmitResult: (quizId: Long, score: Int, weakTopics: String) -> Unit,
    onDeleteQuiz: (Long) -> Unit,
    lang: String = "en"
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    if (activeQuiz != null) {
        QuizTakerView(
            quiz = activeQuiz,
            onBack = { onSelectQuiz(null) },
            onSubmitScore = { score, weakTopics ->
                onSubmitResult(activeQuiz.id, score, weakTopics)
            },
            lang = lang
        )
    } else {
        // Quizzes List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .testTag("quiz_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = AppStrings.get("quiz_title", lang),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Test your comprehension with adaptive AI diagnostics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showCreateDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_new_quiz")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Quiz")
                    }
                }
            }

            if (quizzes.isEmpty()) {
                item {
                    Text(
                        text = "No quizzes yet. Tap 'New Quiz' to test your knowledge on any topic!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(quizzes) { quiz ->
                    QuizCardItem(
                        quiz = quiz,
                        onOpen = { onSelectQuiz(quiz) },
                        onDelete = { onDeleteQuiz(quiz.id) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateQuizDialog(
            isGenerating = isGeneratingQuiz,
            defaultSubject = userProfile?.major ?: "General",
            onDismiss = { showCreateDialog = false },
            onGenerate = { subject, topic, count, difficulty, type ->
                onGenerateQuiz(subject, topic, count, difficulty, type)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun QuizCardItem(
    quiz: QuizEntity,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("quiz_item_${quiz.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${quiz.subject} • ${quiz.difficulty}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (quiz.isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${quiz.scorePercentage}%",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF047857),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = quiz.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Topic: ${quiz.topic} • ${quiz.questionType}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuizTakerView(
    quiz: QuizEntity,
    onBack: () -> Unit,
    onSubmitScore: (score: Int, weakTopics: String) -> Unit,
    lang: String
) {
    val questions = remember {
        val list = mutableListOf<QuizQuestionResult>()
        try {
            val arr = JSONArray(quiz.questionsJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val opts = mutableListOf<String>()
                val optsArr = obj.optJSONArray("options")
                if (optsArr != null) {
                    for (j in 0 until optsArr.length()) opts.add(optsArr.getString(j))
                }
                list.add(
                    QuizQuestionResult(
                        id = obj.optInt("id", i + 1),
                        question = obj.optString("question"),
                        options = opts,
                        correctIndex = obj.optInt("correctIndex", 0),
                        explanation = obj.optString("explanation")
                    )
                )
            }
        } catch (e: Exception) {
            list.add(QuizQuestionResult(1, "Sample question", listOf("A", "B", "C", "D"), 0, "Default"))
        }
        list
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    val userAnswers = remember { mutableStateMapOf<Int, Int>() } // question index -> chosen option index
    var isSubmitted by remember { mutableStateOf(quiz.isCompleted) }

    val currentQ = questions.getOrNull(currentIndex)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("quiz_taker_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = quiz.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Question ${currentIndex + 1} of ${questions.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        if (!isSubmitted && currentQ != null) {
            // Active Question Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = currentQ.question,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, lineHeight = 24.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Options
                        val selectedOpt = userAnswers[currentIndex]
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            currentQ.options.forEachIndexed { optIdx, optText ->
                                val isSelected = selectedOpt == optIdx
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { userAnswers[currentIndex] = optIdx }
                                        .testTag("option_$optIdx")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${'A' + optIdx}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = optText,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Controls
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { if (currentIndex > 0) currentIndex-- },
                        enabled = currentIndex > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev")
                    }

                    if (currentIndex == questions.size - 1) {
                        Button(
                            onClick = {
                                var correctCount = 0
                                questions.forEachIndexed { idx, q ->
                                    if (userAnswers[idx] == q.correctIndex) correctCount++
                                }
                                val score = ((correctCount.toFloat() / questions.size) * 100).toInt()
                                val weak = if (score < 80) "Review fundamental definitions and step derivations in ${quiz.topic}" else "Mastery demonstrated!"
                                isSubmitted = true
                                onSubmitScore(score, weak)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_submit_quiz")
                        ) {
                            Text(AppStrings.get("btn_submit", lang))
                        }
                    } else {
                        Button(
                            onClick = { if (currentIndex < questions.size - 1) currentIndex++ },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        } else {
            // Final Diagnostic Report View
            item {
                var correctCount = 0
                questions.forEachIndexed { idx, q ->
                    if (userAnswers[idx] == q.correctIndex) correctCount++
                }
                val score = ((correctCount.toFloat() / questions.size.coerceAtLeast(1)) * 100).toInt()

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("quiz_results_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$score%",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (score >= 80) "Outstanding Performance!" else if (score >= 60) "Good Effort!" else "Needs Targeted Review",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Correct: $correctCount / ${questions.size} questions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Weak Topics / Recommendations
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.get("weak_topics", lang),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = quiz.weakTopics?.ifBlank { "Focus on multi-step algebra and boundary conditions for this topic." }
                                ?: "Focus on multi-step algebra and boundary conditions for this topic.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Question Breakdown List with explanations
            items(questions) { q ->
                val chosen = userAnswers[questions.indexOf(q)]
                val isCorrect = chosen == q.correctIndex
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCorrect) Color(0xFF10B981).copy(alpha = 0.08f) else Color(0xFFEF4444).copy(alpha = 0.08f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isCorrect) Color(0xFF047857) else Color(0xFFDC2626)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = q.question,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Correct Answer: ${q.options.getOrNull(q.correctIndex) ?: ""}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF047857)
                        )
                        Text(
                            text = "Explanation: ${q.explanation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreateQuizDialog(
    isGenerating: Boolean,
    defaultSubject: String,
    onDismiss: () -> Unit,
    onGenerate: (subject: String, topic: String, count: Int, difficulty: String, type: String) -> Unit
) {
    var subject by remember { mutableStateOf(defaultSubject) }
    var topic by remember { mutableStateOf("") }
    var count by remember { mutableIntStateOf(5) }
    var difficulty by remember { mutableStateOf("Medium") }
    var type by remember { mutableStateOf("MCQ") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate AI Academic Quiz", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    placeholder = { Text("Subject (e.g. Physics, Calculus, Economics)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_quiz_subject")
                )
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    placeholder = { Text("Topic (e.g. Electric Fields & Potential)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_quiz_topic")
                )

                // Count
                Text("Questions Count:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 20).forEach { c ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (count == c) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { count = c }
                        ) {
                            Text(
                                text = "$c",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (count == c) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Difficulty
                Text("Difficulty:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Easy", "Medium", "Hard").forEach { d ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (difficulty == d) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { difficulty = d }
                        ) {
                            Text(
                                text = d,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (difficulty == d) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onGenerate(subject, topic, count, difficulty, type) },
                enabled = !isGenerating && topic.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_generate_quiz")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                } else {
                    Text("Generate Quiz")
                }
            }
        },
        dismissButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
