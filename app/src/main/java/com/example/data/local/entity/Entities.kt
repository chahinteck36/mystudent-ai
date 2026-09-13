package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Alex Morgan",
    val email: String = "student@studymate.ai",
    val educationLevel: String = "Undergraduate",
    val major: String = "Computer Science & Engineering",
    val subjects: String = "Calculus, Physics, Algorithms, Data Structures",
    val learningPreferences: String = "Step-by-step explanations, Practice questions",
    val mainGoal: String = "Improve grades & master core concepts",
    val plan: String = "Free", // Free or Pro
    val questionsSolvedCount: Int = 14,
    val quizzesCompletedCount: Int = 8,
    val flashcardsReviewedCount: Int = 42,
    val studySessionsCount: Int = 19,
    val learningStreakDays: Int = 6,
    val aiRequestsUsedToday: Int = 7,
    val aiRequestsLimitToday: Int = 15,
    val isOnboardingCompleted: Boolean = true,
    val selectedLanguage: String = "en", // "en", "ar", "fr"
    val isDarkMode: Boolean = false
)

@Entity(tableName = "problem_solutions")
data class ProblemSolutionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val problemText: String,
    val subject: String,
    val understanding: String,
    val givenInfo: String,
    val solutionSteps: String, // Step 1: ... | Step 2: ...
    val finalAnswer: String,
    val explanation: String,
    val checkQuestion: String,
    val checkAnswer: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageBase64: String? = null
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String = "main",
    val sender: String, // "user" or "ai"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "lesson_summaries")
data class LessonSummaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val lengthType: String = "standard", // short, standard, detailed
    val overview: String,
    val keyConcepts: String,
    val definitions: String,
    val formulas: String,
    val examples: String,
    val thingsToRemember: String,
    val quickReview: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "flashcard_decks")
data class FlashcardDeckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val cardsJson: String, // serialized JSON list
    val totalCards: Int,
    val masteredCards: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val topic: String,
    val difficulty: String, // Easy, Medium, Hard
    val questionType: String, // MCQ, True/False, Mixed
    val questionsJson: String,
    val scorePercentage: Int? = null,
    val isCompleted: Boolean = false,
    val weakTopics: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "lecture_notes")
data class LectureNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val durationSeconds: Int,
    val transcript: String,
    val summary: String,
    val keyPoints: String,
    val studyNotes: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "library_items")
data class LibraryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val fileType: String, // PDF, Image, Audio, Document
    val sizeLabel: String,
    val timestamp: Long = System.currentTimeMillis(),
    val contentSummary: String
)
