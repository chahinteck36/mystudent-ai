package com.example.data.ai

data class ProblemSolutionResult(
    val problemText: String,
    val subject: String,
    val understanding: String,
    val givenInfo: String,
    val solutionSteps: String,
    val finalAnswer: String,
    val explanation: String,
    val checkQuestion: String,
    val checkAnswer: String
)

data class QuizQuestionResult(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class FlashcardItemResult(
    val question: String,
    val answer: String,
    val difficulty: String = "Medium"
)

data class SummaryResult(
    val title: String,
    val subject: String,
    val overview: String,
    val keyConcepts: String,
    val definitions: String,
    val formulas: String,
    val examples: String,
    val thingsToRemember: String,
    val quickReview: String
)

data class LectureResult(
    val summary: String,
    val keyPoints: List<String>,
    val importantTerms: List<String>,
    val questions: List<String>,
    val studyNotes: String
)
