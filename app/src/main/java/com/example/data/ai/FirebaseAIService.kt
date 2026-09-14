package com.example.data.ai

import android.util.Base64
import android.util.Log
import com.example.data.local.entity.UserProfileEntity
import com.google.firebase.Firebase
import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.InlineDataPart
import com.google.firebase.ai.type.TextPart
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Production-ready AI service powered directly by Firebase AI Logic
 * using the Gemini Developer API provider backend (Google AI).
 * No raw Gemini API key is needed or exposed on client.
 */
class FirebaseAIService(
    modelName: String = "gemini-2.5-flash"
) : AIService {

    private val generativeModel: GenerativeModel by lazy {
        val firebaseAI: FirebaseAI = Firebase.ai(backend = GenerativeBackend.googleAI())
        firebaseAI.generativeModel(
            modelName = modelName,
            generationConfig = generationConfig {
                temperature = 0.3f
                topP = 0.95f
            }
        )
    }

    private suspend fun generateContentWithFirebaseAI(
        systemInstruction: String,
        userPrompt: String,
        imageBase64: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val parts = mutableListOf<com.google.firebase.ai.type.Part>()

            val combinedPrompt = buildString {
                append("System Instruction:\n")
                append(systemInstruction.trimIndent())
                append("\n\nUser Request:\n")
                append(userPrompt)
            }
            parts.add(TextPart(combinedPrompt))

            if (!imageBase64.isNullOrBlank()) {
                try {
                    val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                    parts.add(InlineDataPart(imageBytes, "image/jpeg"))
                } catch (e: Exception) {
                    Log.w("FirebaseAIService", "Failed to decode image Base64", e)
                }
            }

            val content = Content("user", parts)
            val response = generativeModel.generateContent(content)
            val text = response.text

            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.failure(IllegalStateException("Empty response from Firebase AI"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseAIService", "Firebase AI generation error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun solveProblem(
        problemText: String,
        imageBase64: String?,
        userProfile: UserProfileEntity
    ): Result<ProblemSolutionResult> {
        val systemPrompt = """
            You are StudyMate AI's elite Academic Problem Solver.
            The user is an ${userProfile.educationLevel} student majoring in ${userProfile.major}.
            Preferred style: ${userProfile.learningPreferences}.
            Solve the problem step-by-step with extreme clarity. Return pure JSON with keys:
            - "subject": academic field (e.g. Mathematics, Physics, Chemistry, Computer Science)
            - "understanding": concise explanation of what the question asks
            - "givenInfo": explicit parameters, constants, and known values
            - "solutionSteps": detailed step-by-step deduction (e.g. Step 1: ..., Step 2: ...)
            - "finalAnswer": bold/clear final outcome
            - "explanation": pedagogical insight and common pitfalls
            - "checkQuestion": a short comprehension test question
            - "checkAnswer": the answer to the check question
        """.trimIndent()

        val prompt = "Solve this academic problem in depth:\n$problemText"

        val aiResult = generateContentWithFirebaseAI(systemPrompt, prompt, imageBase64)
        if (aiResult.isSuccess) {
            val text = aiResult.getOrNull().orEmpty()
            try {
                val cleaned = text.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()
                val json = JSONObject(cleaned)
                return Result.success(
                    ProblemSolutionResult(
                        problemText = problemText,
                        subject = json.optString("subject", "General Academic"),
                        understanding = json.optString("understanding", "Solve for the requested variables."),
                        givenInfo = json.optString("givenInfo", "Identified from problem text."),
                        solutionSteps = json.optString("solutionSteps", text),
                        finalAnswer = json.optString("finalAnswer", "See step-by-step solution."),
                        explanation = json.optString("explanation", "Review the step-by-step derivation above."),
                        checkQuestion = json.optString("checkQuestion", ""),
                        checkAnswer = json.optString("checkAnswer", "")
                    )
                )
            } catch (e: Exception) {
                Log.w("FirebaseAIService", "Failed to parse JSON, building structured result from text", e)
                return Result.success(
                    ProblemSolutionResult(
                        problemText = problemText,
                        subject = "Academic Study",
                        understanding = "Analysis of requested problem.",
                        givenInfo = problemText,
                        solutionSteps = text,
                        finalAnswer = "Solution provided above.",
                        explanation = "Generated via Gemini.",
                        checkQuestion = "",
                        checkAnswer = ""
                    )
                )
            }
        }

        return Result.failure(aiResult.exceptionOrNull() ?: Exception("Failed to solve problem. Please check your connection and try again."))
    }

    override suspend fun analyzeImage(imageBase64: String): Result<String> {
        val systemPrompt = "Extract and transcribe all text, mathematical formulas, and academic symbols from this image accurately."
        return generateContentWithFirebaseAI(systemPrompt, "Transcribe all academic content from this image.", imageBase64)
    }

    override suspend fun summarizeText(
        text: String,
        length: String,
        userProfile: UserProfileEntity
    ): Result<SummaryResult> {
        val systemPrompt = """
            You are StudyMate AI's Lesson Summarizer.
            Student level: ${userProfile.educationLevel}. Major: ${userProfile.major}.
            Summary depth requested: $length.
            Return a JSON object with:
            - "title": lesson title
            - "subject": academic subject
            - "overview": high-level synopsis
            - "keyConcepts": bullet points of main themes
            - "definitions": critical terms defined
            - "formulas": key equations/rules
            - "examples": illustrative concrete application
            - "thingsToRemember": common mistakes and core insights
            - "quickReview": checklist to review before exams
        """.trimIndent()

        val aiResult = generateContentWithFirebaseAI(systemPrompt, "Summarize this educational content:\n$text")
        if (aiResult.isSuccess) {
            val raw = aiResult.getOrNull().orEmpty()
            try {
                val cleaned = raw.trim()
                    .removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val json = JSONObject(cleaned)
                return Result.success(
                    SummaryResult(
                        title = json.optString("title", "Lesson Summary"),
                        subject = json.optString("subject", "Academic Studies"),
                        overview = json.optString("overview", ""),
                        keyConcepts = json.optString("keyConcepts", ""),
                        definitions = json.optString("definitions", ""),
                        formulas = json.optString("formulas", ""),
                        examples = json.optString("examples", ""),
                        thingsToRemember = json.optString("thingsToRemember", ""),
                        quickReview = json.optString("quickReview", "")
                    )
                )
            } catch (e: Exception) {
                Log.w("FirebaseAIService", "JSON parsing failed for summary, using raw text", e)
                return Result.success(
                    SummaryResult(
                        title = "Summary",
                        subject = userProfile.major.ifBlank { "Academic Studies" },
                        overview = raw,
                        keyConcepts = "",
                        definitions = "",
                        formulas = "",
                        examples = "",
                        thingsToRemember = "",
                        quickReview = ""
                    )
                )
            }
        }

        return Result.failure(aiResult.exceptionOrNull() ?: Exception("Failed to generate summary. Please try again."))
    }

    override suspend fun generateFlashcards(
        content: String,
        count: Int,
        userProfile: UserProfileEntity
    ): Result<List<FlashcardItemResult>> {
        val systemPrompt = """
            You are StudyMate AI's Flashcard Generator.
            Create $count high-yield flashcard questions and answers for an ${userProfile.educationLevel} student.
            Return a JSON array of objects with keys: "question", "answer", "difficulty" (Easy/Medium/Hard).
        """.trimIndent()

        val aiResult = generateContentWithFirebaseAI(systemPrompt, "Create flashcards for:\n$content")
        if (aiResult.isSuccess) {
            try {
                val cleaned = aiResult.getOrNull().orEmpty().trim()
                    .removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val arr = JSONArray(cleaned)
                val list = mutableListOf<FlashcardItemResult>()
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    list.add(
                        FlashcardItemResult(
                            question = item.optString("question"),
                            answer = item.optString("answer"),
                            difficulty = item.optString("difficulty", "Medium")
                        )
                    )
                }
                if (list.isNotEmpty()) return Result.success(list)
            } catch (e: Exception) {
                Log.w("FirebaseAIService", "Failed to parse flashcard JSON", e)
                return Result.failure(Exception("Failed to format flashcards from AI response. Please try again."))
            }
        }

        return Result.failure(aiResult.exceptionOrNull() ?: Exception("Failed to generate flashcards. Please try again."))
    }

    override suspend fun generateQuiz(
        subject: String,
        topic: String,
        count: Int,
        difficulty: String,
        questionType: String,
        userProfile: UserProfileEntity
    ): Result<List<QuizQuestionResult>> {
        val systemPrompt = """
            You are StudyMate AI's Academic Quiz Generator.
            Generate $count $difficulty $questionType quiz questions on "$topic" in $subject.
            Return a JSON array of objects, each containing:
            - "id": integer 1 to $count
            - "question": clear question statement
            - "options": array of 4 distinct choices
            - "correctIndex": index 0-3 of the correct choice
            - "explanation": explanation of why that answer is correct
        """.trimIndent()

        val aiResult = generateContentWithFirebaseAI(systemPrompt, "Generate a $difficulty quiz about $topic ($subject).")
        if (aiResult.isSuccess) {
            try {
                val cleaned = aiResult.getOrNull().orEmpty().trim()
                    .removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val arr = JSONArray(cleaned)
                val list = mutableListOf<QuizQuestionResult>()
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    val opts = mutableListOf<String>()
                    val optsJson = item.optJSONArray("options")
                    if (optsJson != null) {
                        for (j in 0 until optsJson.length()) {
                            opts.add(optsJson.getString(j))
                        }
                    }
                    list.add(
                        QuizQuestionResult(
                            id = item.optInt("id", i + 1),
                            question = item.optString("question"),
                            options = if (opts.isNotEmpty()) opts else listOf("Option A", "Option B", "Option C", "Option D"),
                            correctIndex = item.optInt("correctIndex", 0),
                            explanation = item.optString("explanation", "Based on core theoretical principles.")
                        )
                    )
                }
                if (list.isNotEmpty()) return Result.success(list)
            } catch (e: Exception) {
                Log.w("FirebaseAIService", "Quiz parsing failed", e)
                return Result.failure(Exception("Failed to parse quiz questions from AI response. Please try again."))
            }
        }

        return Result.failure(aiResult.exceptionOrNull() ?: Exception("Failed to generate quiz. Please try again."))
    }

    override suspend fun chatTutor(
        userMessage: String,
        history: List<Pair<String, String>>,
        userProfile: UserProfileEntity
    ): Result<String> {
        val systemPrompt = """
            You are "StudyMate Tutor", a knowledgeable, patient, and pedagogically sound academic AI coach.
            Student profile: Level: ${userProfile.educationLevel}, Major: ${userProfile.major}.
            Subjects: ${userProfile.subjects}.
            Preferred style: ${userProfile.learningPreferences}.
            Rules:
            1. Never claim to be a human teacher; you are StudyMate Tutor.
            2. Break down complex steps clearly and encourage the student.
            3. Use concrete real-world examples.
            4. If the user asks in Arabic, reply in fluent, natural Arabic. If in French, reply in French. Otherwise English.
            5. Provide clear, supportive academic reasoning.
        """.trimIndent()

        val historyText = buildString {
            history.takeLast(6).forEach { (sender, msg) ->
                append(if (sender == "user") "Student: " else "Tutor: ")
                append(msg)
                append("\n")
            }
            append("Student: $userMessage\nTutor: ")
        }

        return generateContentWithFirebaseAI(systemPrompt, historyText)
    }

    override suspend fun analyzeLecture(
        transcript: String,
        userProfile: UserProfileEntity
    ): Result<LectureResult> {
        val systemPrompt = """
            Analyze this lecture recording transcript for an ${userProfile.educationLevel} student in ${userProfile.major}.
            Return JSON with:
            - "summary": comprehensive overview of the lecture
            - "keyPoints": array of 4-6 crucial takeaways
            - "importantTerms": array of technical terms with concise definitions
            - "questions": array of 3 self-test questions
            - "studyNotes": organized structured revision notes
        """.trimIndent()

        val aiResult = generateContentWithFirebaseAI(systemPrompt, transcript)
        if (aiResult.isSuccess) {
            try {
                val cleaned = aiResult.getOrNull().orEmpty().trim()
                    .removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val json = JSONObject(cleaned)
                val keyPointsList = mutableListOf<String>()
                json.optJSONArray("keyPoints")?.let { arr ->
                    for (i in 0 until arr.length()) keyPointsList.add(arr.getString(i))
                }
                val termsList = mutableListOf<String>()
                json.optJSONArray("importantTerms")?.let { arr ->
                    for (i in 0 until arr.length()) termsList.add(arr.getString(i))
                }
                val questionsList = mutableListOf<String>()
                json.optJSONArray("questions")?.let { arr ->
                    for (i in 0 until arr.length()) questionsList.add(arr.getString(i))
                }

                return Result.success(
                    LectureResult(
                        summary = json.optString("summary"),
                        keyPoints = keyPointsList,
                        importantTerms = termsList,
                        questions = questionsList,
                        studyNotes = json.optString("studyNotes")
                    )
                )
            } catch (e: Exception) {
                Log.w("FirebaseAIService", "Lecture parsing error", e)
                return Result.failure(Exception("Failed to parse lecture notes from AI response. Please try again."))
            }
        }

        return Result.failure(aiResult.exceptionOrNull() ?: Exception("Failed to analyze lecture transcript. Please try again."))
    }

    override suspend fun verifyGeminiConnection(testPrompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d("FirebaseAIService", "Executing Firebase AI Logic runtime verification with prompt: $testPrompt")
            val response = generativeModel.generateContent(testPrompt)
            val text = response.text
            if (!text.isNullOrBlank()) {
                Log.d("FirebaseAIService", "Received Gemini response via Firebase AI Logic successfully (${text.length} chars)")
                Result.success(text.trim())
            } else {
                Result.failure(IllegalStateException("Firebase AI returned an empty response"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseAIService", "Firebase AI verification request failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}
