package com.example.data.ai

import com.example.data.local.entity.UserProfileEntity

interface AIService {
    suspend fun solveProblem(
        problemText: String,
        imageBase64: String? = null,
        userProfile: UserProfileEntity
    ): Result<ProblemSolutionResult>

    suspend fun analyzeImage(
        imageBase64: String
    ): Result<String>

    suspend fun summarizeText(
        text: String,
        length: String, // "short", "standard", "detailed"
        userProfile: UserProfileEntity
    ): Result<SummaryResult>

    suspend fun generateFlashcards(
        content: String,
        count: Int,
        userProfile: UserProfileEntity
    ): Result<List<FlashcardItemResult>>

    suspend fun generateQuiz(
        subject: String,
        topic: String,
        count: Int,
        difficulty: String,
        questionType: String,
        userProfile: UserProfileEntity
    ): Result<List<QuizQuestionResult>>

    suspend fun chatTutor(
        userMessage: String,
        history: List<Pair<String, String>>,
        userProfile: UserProfileEntity
    ): Result<String>

    suspend fun analyzeLecture(
        transcript: String,
        userProfile: UserProfileEntity
    ): Result<LectureResult>

    suspend fun verifyGeminiConnection(
        testPrompt: String = "Explain photosynthesis in one short paragraph."
    ): Result<String>
}
