package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.StudyMateDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.FlashcardDeckEntity
import com.example.data.local.entity.LectureNoteEntity
import com.example.data.local.entity.LessonSummaryEntity
import com.example.data.local.entity.LibraryItemEntity
import com.example.data.local.entity.ProblemSolutionEntity
import com.example.data.local.entity.QuizEntity
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfileEntity::class,
        ProblemSolutionEntity::class,
        ChatMessageEntity::class,
        LessonSummaryEntity::class,
        FlashcardDeckEntity::class,
        QuizEntity::class,
        LectureNoteEntity::class,
        LibraryItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studyMateDao(): StudyMateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "studymate_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.studyMateDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: StudyMateDao) {
            // Initial User Profile
            dao.insertOrUpdateProfile(
                UserProfileEntity(
                    id = 1,
                    name = "Karim Al-Hassan",
                    email = "karim.study@example.com",
                    educationLevel = "Undergraduate",
                    major = "Computer Science & Engineering",
                    subjects = "Calculus II, Physics (Electromagnetism), Data Structures, Linear Algebra",
                    learningPreferences = "Step-by-step explanations, Practice questions",
                    mainGoal = "Pass exams with high distinction & master problem solving",
                    plan = "Free",
                    questionsSolvedCount = 18,
                    quizzesCompletedCount = 6,
                    flashcardsReviewedCount = 64,
                    studySessionsCount = 22,
                    learningStreakDays = 5,
                    aiRequestsUsedToday = 4,
                    aiRequestsLimitToday = 15,
                    isOnboardingCompleted = true,
                    selectedLanguage = "en",
                    isDarkMode = false
                )
            )

            // Initial Sample Problem Solution
            dao.insertSolution(
                ProblemSolutionEntity(
                    problemText = "Find the derivative of f(x) = x^3 * e^(2x) using the product rule.",
                    subject = "Mathematics (Calculus)",
                    understanding = "The problem requires finding the first derivative of a product of two functions: a cubic polynomial and an exponential function.",
                    givenInfo = "f(x) = u(x) * v(x) where u(x) = x³ and v(x) = e^(2x)",
                    solutionSteps = "Step 1: Identify components: u = x³, v = e^(2x)\nStep 2: Differentiate u: u' = 3x²\nStep 3: Differentiate v using chain rule: v' = 2e^(2x)\nStep 4: Apply product rule: f'(x) = u'v + uv'\nStep 5: Substitute: f'(x) = 3x² * e^(2x) + x³ * 2e^(2x)\nStep 6: Factor common term x² * e^(2x): f'(x) = x² * e^(2x) * (3 + 2x)",
                    finalAnswer = "f'(x) = x² e^(2x) (2x + 3)",
                    explanation = "Whenever you multiply two variable expressions, you cannot simply multiply their derivatives. You must use the Product Rule: (uv)' = u'v + uv'. Then, factor out common algebraic terms for the cleanest form.",
                    checkQuestion = "What would be the derivative of g(x) = x² * e^(3x)?",
                    checkAnswer = "g'(x) = x * e^(3x) * (3x + 2)"
                )
            )

            dao.insertSolution(
                ProblemSolutionEntity(
                    problemText = "Calculate the total resistance and current in a circuit with a 12V battery connected to three resistors in series: 4Ω, 6Ω, and 2Ω.",
                    subject = "Physics (Circuits)",
                    understanding = "Determine the equivalent resistance of resistors in a series configuration and apply Ohm's Law to calculate circuit current.",
                    givenInfo = "Voltage V = 12V, R₁ = 4Ω, R₂ = 6Ω, R₃ = 2Ω",
                    solutionSteps = "Step 1: In a series circuit, resistances add directly: R_total = R₁ + R₂ + R₃\nStep 2: R_total = 4 + 6 + 2 = 12 Ω\nStep 3: Apply Ohm's Law: I = V / R_total\nStep 4: I = 12V / 12Ω = 1.0 A",
                    finalAnswer = "R_total = 12 Ω, Current I = 1.0 A",
                    explanation = "In series circuits, the same electric charge flows consecutively through each resistor, so their resistances sum up. The current is constant throughout.",
                    checkQuestion = "If the resistors were connected in parallel instead, would total resistance be greater or smaller than 2Ω?",
                    checkAnswer = "Smaller! In parallel, the equivalent resistance is always strictly less than the smallest individual resistor (here < 2Ω)."
                )
            )

            // Initial Chat Messages with StudyMate Tutor
            dao.insertChatMessage(
                ChatMessageEntity(
                    conversationId = "main",
                    sender = "ai",
                    message = "Hello! I am **StudyMate Tutor**, your personal academic partner. What topic or assignment are we working on today? Feel free to ask me to explain a concept step by step, solve a problem, or quiz your knowledge!"
                )
            )

            // Initial Summary
            dao.insertSummary(
                LessonSummaryEntity(
                    title = "Newton's Laws of Motion & Dynamics",
                    subject = "Physics",
                    lengthType = "standard",
                    overview = "Newton's laws of motion form the fundamental foundation of classical mechanics, describing the exact relationship between the forces acting on a body and its resulting motion through space.",
                    keyConcepts = "• Inertia & Reference Frames\n• Net Force & Acceleration (F = ma)\n• Action-Reaction Pairs (Equal & Opposite Forces)\n• Friction, Normal Force, and Tension",
                    definitions = "• Inertia: The natural tendency of an object to resist changes in its velocity.\n• Mass: A quantitative measure of an object's inertia (SI unit: kg).\n• Net Force: The vector sum of all individual forces acting simultaneously on a body.",
                    formulas = "• First Law: ∑F = 0 ⟹ v = constant (Equilibrium)\n• Second Law: ∑F = m * a\n• Weight: W = m * g (where g ≈ 9.81 m/s²)\n• Friction: f_k = μ_k * F_N",
                    examples = "A 1000 kg car accelerates at 2.5 m/s². The net force required is F = 1000 * 2.5 = 2,500 N.",
                    thingsToRemember = "Action and reaction forces act on DIFFERENT objects, which is why they never cancel each other out in a single body's free body diagram!",
                    quickReview = "1. Net force determines acceleration, not velocity.\n2. In space with no friction, an object keeps moving forever without an engine.\n3. Always draw a Free Body Diagram (FBD) before calculating."
                )
            )

            // Initial Flashcards Deck
            dao.insertDeck(
                FlashcardDeckEntity(
                    title = "Essential Data Structures & Algorithms",
                    subject = "Computer Science",
                    cardsJson = """[
                        {"question": "What is the worst-case time complexity of QuickSort?", "answer": "O(n²), which occurs when the pivot chosen is consistently the smallest or largest element.", "difficulty": "Medium", "isMastered": false},
                        {"question": "What is the primary difference between a Stack and a Queue?", "answer": "A Stack follows LIFO (Last In, First Out), while a Queue follows FIFO (First In, First Out).", "difficulty": "Easy", "isMastered": true},
                        {"question": "What is the average lookup time in a Hash Table with good distribution?", "answer": "O(1) constant time on average.", "difficulty": "Easy", "isMastered": true},
                        {"question": "What is Dijkstra's algorithm used for?", "answer": "Finding the shortest path from a single source node to all other nodes in a graph with non-negative edge weights.", "difficulty": "Hard", "isMastered": false},
                        {"question": "What makes a Binary Search Tree (BST) balanced in an AVL Tree?", "answer": "The heights of the two child subtrees of any node differ by at most one, enforced via tree rotations.", "difficulty": "Hard", "isMastered": false}
                    ]""".trimIndent(),
                    totalCards = 5,
                    masteredCards = 2
                )
            )

            // Initial Quiz
            dao.insertQuiz(
                QuizEntity(
                    title = "Calculus & Limits Diagnostic",
                    subject = "Mathematics",
                    topic = "Limits and Continuity",
                    difficulty = "Medium",
                    questionType = "MCQ",
                    questionsJson = """[
                        {
                            "id": 1,
                            "question": "What is the limit of (sin x) / x as x approaches 0?",
                            "options": ["0", "1", "Infinity", "Does not exist"],
                            "correctIndex": 1,
                            "explanation": "This is a fundamental trigonometric limit: lim(x->0) sin(x)/x = 1. It can also be verified using L'Hôpital's rule: cos(0)/1 = 1."
                        },
                        {
                            "id": 2,
                            "question": "If f(x) is continuous at x = c, which condition must hold true?",
                            "options": ["f'(c) must exist", "lim(x->c) f(x) = f(c)", "f(c) = 0", "f''(c) > 0"],
                            "correctIndex": 1,
                            "explanation": "A function is continuous at c if the limit as x approaches c exists and is strictly equal to f(c)."
                        },
                        {
                            "id": 3,
                            "question": "What is lim(x->∞) (3x² + 5) / (2x² - 7)?",
                            "options": ["5/7", "3/2", "Infinity", "0"],
                            "correctIndex": 1,
                            "explanation": "Divide the numerator and denominator by the highest power of x (x²): (3 + 5/x²) / (2 - 7/x²) -> 3/2 as x -> ∞."
                        }
                    ]""".trimIndent(),
                    scorePercentage = 100,
                    isCompleted = true,
                    weakTopics = "None - Excellent grasp of fundamental limits!"
                )
            )

            // Initial Library Item
            dao.insertLibraryItem(
                LibraryItemEntity(
                    name = "Calculus_II_Syllabus_and_Formulas.pdf",
                    fileType = "PDF",
                    sizeLabel = "2.4 MB",
                    contentSummary = "Integration techniques, partial fractions, series convergence tests, and power series."
                )
            )

            dao.insertLibraryItem(
                LibraryItemEntity(
                    name = "Physics_Electromagnetism_Notes.pdf",
                    fileType = "PDF",
                    sizeLabel = "4.1 MB",
                    contentSummary = "Gauss's law, electric potential, capacitance, Ampere's law, and Maxwell equations."
                )
            )
        }
    }
}
