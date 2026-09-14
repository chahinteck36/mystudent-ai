package com.example.data.repository

import com.example.data.ai.AIService
import com.example.data.ai.FlashcardItemResult
import com.example.data.ai.LectureResult
import com.example.data.ai.ProblemSolutionResult
import com.example.data.ai.QuizQuestionResult
import com.example.data.ai.SummaryResult
import com.example.data.local.dao.StudyMateDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.FlashcardDeckEntity
import com.example.data.local.entity.LectureNoteEntity
import com.example.data.local.entity.LessonSummaryEntity
import com.example.data.local.entity.LibraryItemEntity
import com.example.data.local.entity.ProblemSolutionEntity
import com.example.data.local.entity.QuizEntity
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class StudyMateRepository(
    private val dao: StudyMateDao,
    private val aiService: AIService
) {

    // Profile & Usage
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()

    suspend fun getProfileOnce(): UserProfileEntity {
        return dao.getUserProfileOnce() ?: UserProfileEntity().also {
            dao.insertOrUpdateProfile(it)
        }
    }

    suspend fun updateProfile(profile: UserProfileEntity) {
        dao.insertOrUpdateProfile(profile)
    }

    suspend fun updateUserAccount(name: String, email: String) {
        val current = getProfileOnce()
        dao.insertOrUpdateProfile(
            current.copy(
                name = name,
                email = email
            )
        )
    }

    suspend fun updateAcademicPreferences(
        level: String,
        major: String,
        subjects: String,
        learningPreferences: String,
        mainGoal: String
    ) {
        val current = getProfileOnce()
        dao.insertOrUpdateProfile(
            current.copy(
                educationLevel = level,
                major = major,
                subjects = subjects,
                learningPreferences = learningPreferences,
                mainGoal = mainGoal,
                isOnboardingCompleted = true
            )
        )
    }

    suspend fun toggleDarkMode(isDark: Boolean) {
        val current = getProfileOnce()
        dao.insertOrUpdateProfile(current.copy(isDarkMode = isDark))
    }

    suspend fun setLanguage(lang: String) {
        val current = getProfileOnce()
        dao.insertOrUpdateProfile(current.copy(selectedLanguage = lang))
    }

    suspend fun upgradeToPro() {
        val current = getProfileOnce()
        dao.insertOrUpdateProfile(
            current.copy(
                plan = "Pro",
                aiRequestsLimitToday = 999
            )
        )
    }

    private suspend fun incrementAiUsage() {
        val current = getProfileOnce()
        val newUsage = current.aiRequestsUsedToday + 1
        dao.insertOrUpdateProfile(current.copy(aiRequestsUsedToday = newUsage))
    }

    // Problem Solver
    val allSolutions: Flow<List<ProblemSolutionEntity>> = dao.getAllSolutions()

    suspend fun solveAndSaveProblem(problemText: String, imageBase64: String? = null): Result<ProblemSolutionEntity> {
        val profile = getProfileOnce()
        val result = aiService.solveProblem(problemText, imageBase64, profile)
        return if (result.isSuccess) {
            val sol = result.getOrThrow()
            val entity = ProblemSolutionEntity(
                problemText = sol.problemText,
                subject = sol.subject,
                understanding = sol.understanding,
                givenInfo = sol.givenInfo,
                solutionSteps = sol.solutionSteps,
                finalAnswer = sol.finalAnswer,
                explanation = sol.explanation,
                checkQuestion = sol.checkQuestion,
                checkAnswer = sol.checkAnswer,
                imageBase64 = imageBase64
            )
            val id = dao.insertSolution(entity)
            incrementAiUsage()
            val updated = getProfileOnce()
            dao.insertOrUpdateProfile(updated.copy(questionsSolvedCount = updated.questionsSolvedCount + 1))
            Result.success(entity.copy(id = id))
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to solve problem"))
        }
    }

    suspend fun deleteSolution(id: Long) = dao.deleteSolution(id)

    // Chat / AI Tutor
    val chatMessages: Flow<List<ChatMessageEntity>> = dao.getChatMessages("main")

    suspend fun sendChatMessage(userText: String): Result<String> {
        val profile = getProfileOnce()
        // Save user message
        dao.insertChatMessage(
            ChatMessageEntity(
                conversationId = "main",
                sender = "user",
                message = userText
            )
        )

        // Generate response from AI
        val history = listOf<Pair<String, String>>() // Simplified
        val aiResult = aiService.chatTutor(userText, history, profile)
        return if (aiResult.isSuccess) {
            val responseText = aiResult.getOrThrow()
            dao.insertChatMessage(
                ChatMessageEntity(
                    conversationId = "main",
                    sender = "ai",
                    message = responseText
                )
            )
            incrementAiUsage()
            Result.success(responseText)
        } else {
            val errMessage = aiResult.exceptionOrNull()?.localizedMessage ?: "Unknown error"
            val errText = "Error connecting to AI service: $errMessage"
            dao.insertChatMessage(
                ChatMessageEntity(
                    conversationId = "main",
                    sender = "ai",
                    message = errText
                )
            )
            Result.failure(aiResult.exceptionOrNull() ?: Exception(errText))
        }
    }

    suspend fun clearChat() = dao.clearChatMessages("main")

    // Lesson Summaries
    val allSummaries: Flow<List<LessonSummaryEntity>> = dao.getAllSummaries()

    suspend fun generateAndSaveSummary(
        title: String,
        subject: String,
        text: String,
        length: String
    ): Result<LessonSummaryEntity> {
        val profile = getProfileOnce()
        val result = aiService.summarizeText(text, length, profile)
        return if (result.isSuccess) {
            val s = result.getOrThrow()
            val entity = LessonSummaryEntity(
                title = if (title.isNotBlank()) title else s.title,
                subject = if (subject.isNotBlank()) subject else s.subject,
                lengthType = length,
                overview = s.overview,
                keyConcepts = s.keyConcepts,
                definitions = s.definitions,
                formulas = s.formulas,
                examples = s.examples,
                thingsToRemember = s.thingsToRemember,
                quickReview = s.quickReview
            )
            val id = dao.insertSummary(entity)
            incrementAiUsage()
            Result.success(entity.copy(id = id))
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Summary failed"))
        }
    }

    suspend fun deleteSummary(id: Long) = dao.deleteSummary(id)

    // Flashcards
    val allDecks: Flow<List<FlashcardDeckEntity>> = dao.getAllDecks()

    suspend fun createDeckFromAI(
        title: String,
        subject: String,
        content: String,
        count: Int = 5
    ): Result<FlashcardDeckEntity> {
        val profile = getProfileOnce()
        val result = aiService.generateFlashcards(content, count, profile)
        return if (result.isSuccess) {
            val cards = result.getOrThrow()
            val jsonArr = JSONArray()
            cards.forEach { c ->
                val obj = JSONObject()
                obj.put("question", c.question)
                obj.put("answer", c.answer)
                obj.put("difficulty", c.difficulty)
                obj.put("isMastered", false)
                jsonArr.put(obj)
            }
            val deck = FlashcardDeckEntity(
                title = title,
                subject = subject,
                cardsJson = jsonArr.toString(),
                totalCards = cards.size,
                masteredCards = 0
            )
            val id = dao.insertDeck(deck)
            incrementAiUsage()
            Result.success(deck.copy(id = id))
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to generate cards"))
        }
    }

    suspend fun updateDeckCards(deckId: Long, updatedCardsJson: String, masteredCount: Int) {
        val deck = dao.getDeckById(deckId)
        if (deck != null) {
            dao.updateDeck(
                deck.copy(
                    cardsJson = updatedCardsJson,
                    masteredCards = masteredCount
                )
            )
            val profile = getProfileOnce()
            dao.insertOrUpdateProfile(
                profile.copy(flashcardsReviewedCount = profile.flashcardsReviewedCount + 1)
            )
        }
    }

    suspend fun deleteDeck(id: Long) = dao.deleteDeck(id)

    // Quizzes
    val allQuizzes: Flow<List<QuizEntity>> = dao.getAllQuizzes()

    suspend fun generateQuiz(
        subject: String,
        topic: String,
        count: Int,
        difficulty: String,
        questionType: String
    ): Result<QuizEntity> {
        val profile = getProfileOnce()
        val result = aiService.generateQuiz(subject, topic, count, difficulty, questionType, profile)
        return if (result.isSuccess) {
            val questions = result.getOrThrow()
            val jsonArr = JSONArray()
            questions.forEach { q ->
                val obj = JSONObject()
                obj.put("id", q.id)
                obj.put("question", q.question)
                val optsArr = JSONArray()
                q.options.forEach { optsArr.put(it) }
                obj.put("options", optsArr)
                obj.put("correctIndex", q.correctIndex)
                obj.put("explanation", q.explanation)
                jsonArr.put(obj)
            }
            val quiz = QuizEntity(
                title = "$topic Quiz",
                subject = subject,
                topic = topic,
                difficulty = difficulty,
                questionType = questionType,
                questionsJson = jsonArr.toString()
            )
            val id = dao.insertQuiz(quiz)
            incrementAiUsage()
            Result.success(quiz.copy(id = id))
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Quiz generation failed"))
        }
    }

    suspend fun submitQuizResult(quizId: Long, scorePercent: Int, weakTopics: String) {
        val quiz = dao.getQuizById(quizId)
        if (quiz != null) {
            dao.updateQuiz(
                quiz.copy(
                    scorePercentage = scorePercent,
                    isCompleted = true,
                    weakTopics = weakTopics
                )
            )
            val profile = getProfileOnce()
            dao.insertOrUpdateProfile(
                profile.copy(quizzesCompletedCount = profile.quizzesCompletedCount + 1)
            )
        }
    }

    suspend fun deleteQuiz(id: Long) = dao.deleteQuiz(id)

    // Lecture Notes
    val allLectures: Flow<List<LectureNoteEntity>> = dao.getAllLectures()

    suspend fun processLectureRecording(
        title: String,
        subject: String,
        durationSeconds: Int,
        transcript: String
    ): Result<LectureNoteEntity> {
        val profile = getProfileOnce()
        val result = aiService.analyzeLecture(transcript, profile)
        return if (result.isSuccess) {
            val lecture = result.getOrThrow()
            val entity = LectureNoteEntity(
                title = title,
                subject = subject,
                durationSeconds = durationSeconds,
                transcript = transcript,
                summary = lecture.summary,
                keyPoints = lecture.keyPoints.joinToString("\n• "),
                studyNotes = lecture.studyNotes
            )
            val id = dao.insertLecture(entity)
            incrementAiUsage()
            Result.success(entity.copy(id = id))
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Lecture processing failed"))
        }
    }

    suspend fun deleteLecture(id: Long) = dao.deleteLecture(id)

    // Library Items
    val allLibraryItems: Flow<List<LibraryItemEntity>> = dao.getAllLibraryItems()

    suspend fun addLibraryItem(name: String, type: String, sizeLabel: String, summary: String): Long {
        return dao.insertLibraryItem(
            LibraryItemEntity(
                name = name,
                fileType = type,
                sizeLabel = sizeLabel,
                contentSummary = summary
            )
        )
    }

    suspend fun deleteLibraryItem(id: Long) = dao.deleteLibraryItem(id)

    suspend fun verifyAILogic(testPrompt: String): Result<String> {
        return aiService.verifyGeminiConnection(testPrompt)
    }
}
