package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ai.FirebaseAIService
import com.example.data.local.AppDatabase
import com.example.data.repository.StudyMateRepository
import com.example.ui.components.StudyMateBottomNav
import com.example.ui.components.StudyMateNavDrawer
import com.example.ui.components.StudyMateTopBar
import com.example.ui.screens.admin.AdminScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.flashcards.FlashcardsScreen
import com.example.ui.screens.history.HistoryScreen
import com.example.ui.screens.landing.LandingScreen
import com.example.ui.screens.lectures.LectureRecorderScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.progress.ProgressScreen
import com.example.ui.screens.quiz.QuizScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.data.ai.RegisteredAppCheckProviderFactory
import com.example.ui.screens.solver.ProblemSolverScreen
import com.example.ui.screens.summary.SummaryScreen
import com.example.ui.screens.tutor.AITutorScreen
import com.example.ui.theme.StudyMateTheme
import com.example.ui.viewmodel.StudyMateViewModel
import com.example.ui.viewmodel.StudyMateViewModelFactory
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ensure FirebaseApp is initialized
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        // Configure App Check: use registered debug secret in debug builds, and Play Integrity in release builds
        try {
            val appCheck = FirebaseAppCheck.getInstance()
            if (BuildConfig.DEBUG) {
                val debugSecret = BuildConfig.APP_CHECK_DEBUG_SECRET
                if (debugSecret.isNotBlank()) {
                    appCheck.installAppCheckProviderFactory(
                        RegisteredAppCheckProviderFactory(debugSecret)
                    )
                } else {
                    appCheck.installAppCheckProviderFactory(
                        DebugAppCheckProviderFactory.getInstance()
                    )
                }
            } else {
                // Production / Release: Use Play Integrity attestation
                appCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
            }
        } catch (e: Exception) {
            android.util.Log.w("MainActivity", "Firebase App Check initialization warning: ${e.message}")
        }

        val appDb = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val aiService = FirebaseAIService()
        val repository = StudyMateRepository(appDb.studyMateDao(), aiService)
        val viewModelFactory = StudyMateViewModelFactory(repository)

        setContent {
            val viewModel: StudyMateViewModel = viewModel(factory = viewModelFactory)
            val userProfile by viewModel.userProfile.collectAsState()
            val currentRoute by viewModel.currentRoute.collectAsState()

            val isDark = userProfile?.isDarkMode ?: isSystemInDarkTheme()
            val lang = userProfile?.selectedLanguage ?: "en"
            val layoutDirection = if (lang == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                StudyMateTheme(darkTheme = isDark) {
                    StudyMateApp(
                        viewModel = viewModel,
                        currentRoute = currentRoute,
                        lang = lang
                    )
                }
            }
        }
    }
}

@Composable
fun StudyMateApp(
    viewModel: StudyMateViewModel,
    currentRoute: String,
    lang: String
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Handle back press
    BackHandler(enabled = currentRoute != "dashboard" && currentRoute != "landing") {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            viewModel.navigateTo("dashboard")
        }
    }

    val showBottomNav = currentRoute in listOf("dashboard", "solver", "tutor", "flashcards", "quiz")
    val showTopBar = currentRoute !in listOf("landing", "auth")

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute !in listOf("landing", "auth"),
        drawerContent = {
            StudyMateNavDrawer(
                currentRoute = currentRoute,
                userProfile = userProfile,
                onNavigate = { route ->
                    viewModel.navigateTo(route)
                    coroutineScope.launch { drawerState.close() }
                },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                if (showTopBar) {
                    StudyMateTopBar(
                        currentRoute = currentRoute,
                        userProfile = userProfile,
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                        onLanguageToggle = {
                            val nextLang = when (lang) {
                                "en" -> "ar"
                                "ar" -> "fr"
                                else -> "en"
                            }
                            viewModel.setLanguage(nextLang)
                        },
                        onThemeToggle = {
                            val currentDark = userProfile?.isDarkMode ?: false
                            viewModel.toggleDarkMode(!currentDark)
                        },
                        onAdminClick = {
                            viewModel.navigateTo("admin")
                        }
                    )
                }
            },
            bottomBar = {
                if (showBottomNav) {
                    StudyMateBottomNav(
                        currentRoute = currentRoute,
                        onNavigate = { viewModel.navigateTo(it) },
                        lang = lang
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentRoute) {
                    "landing" -> {
                        LandingScreen(
                            onStartLearning = { viewModel.navigateTo("auth") },
                            onTryTutor = { viewModel.navigateTo("tutor") },
                            onExploreFeature = { feature ->
                                when (feature) {
                                    "solver" -> viewModel.navigateTo("solver")
                                    "summaries" -> viewModel.navigateTo("summary")
                                    "flashcards" -> viewModel.navigateTo("flashcards")
                                    "quiz" -> viewModel.navigateTo("quiz")
                                    "lectures" -> viewModel.navigateTo("lectures")
                                    else -> viewModel.navigateTo("dashboard")
                                }
                            },
                            lang = lang
                        )
                    }

                    "auth" -> {
                        AuthScreen(
                            onAuthSuccess = { _, email, name ->
                                viewModel.updateUserAccount(name, email)
                                val hasCompletedOnboarding = userProfile?.isOnboardingCompleted == true
                                if (hasCompletedOnboarding) {
                                    viewModel.navigateTo("dashboard")
                                } else {
                                    viewModel.navigateTo("onboarding")
                                }
                            },
                            onContinueAsGuest = {
                                viewModel.navigateTo("dashboard")
                            },
                            lang = lang
                        )
                    }

                    "onboarding" -> {
                        OnboardingScreen(
                            onComplete = { level, major, subjects, prefs, goal ->
                                viewModel.updateAcademicPreferences(level, major, subjects, prefs, goal)
                            },
                            lang = lang
                        )
                    }

                    "dashboard" -> {
                        val recentSolutions by viewModel.allSolutions.collectAsState()
                        val recentSummaries by viewModel.allSummaries.collectAsState()
                        val recentQuizzes by viewModel.allQuizzes.collectAsState()

                        DashboardScreen(
                            userProfile = userProfile,
                            recentSolutions = recentSolutions,
                            recentSummaries = recentSummaries,
                            recentQuizzes = recentQuizzes,
                            onActionClick = { actionId ->
                                when (actionId) {
                                    "solver" -> viewModel.navigateTo("solver")
                                    "tutor" -> viewModel.navigateTo("tutor")
                                    "summary" -> viewModel.navigateTo("summary")
                                    "flashcards" -> viewModel.navigateTo("flashcards")
                                    "quiz" -> viewModel.navigateTo("quiz")
                                    "lectures" -> viewModel.navigateTo("lectures")
                                    else -> viewModel.navigateTo("dashboard")
                                }
                            },
                            onSelectSolution = {
                                viewModel.solveProblem(it.problemText, null)
                                viewModel.navigateTo("solver")
                            },
                            onSelectSummary = {
                                viewModel.navigateTo("summary")
                            },
                            onSelectQuiz = {
                                viewModel.selectQuiz(it)
                                viewModel.navigateTo("quiz")
                            },
                            lang = lang
                        )
                    }

                    "solver" -> {
                        val currentSolution by viewModel.currentSolution.collectAsState()
                        val isSolving by viewModel.isSolving.collectAsState()
                        val solverError by viewModel.solverError.collectAsState()

                        ProblemSolverScreen(
                            currentSolution = currentSolution,
                            isSolving = isSolving,
                            solverError = solverError,
                            userProfile = userProfile,
                            onSolve = { text, image -> viewModel.solveProblem(text, image) },
                            onAskTutor = { initialMsg ->
                                viewModel.sendChatMessage(initialMsg)
                                viewModel.navigateTo("tutor")
                            },
                            onCreateFlashcard = { q, a ->
                                viewModel.createDeckFromContent(
                                    title = "Problem Flashcard",
                                    subject = userProfile?.major ?: "Academic",
                                    content = "Q: $q\nA: $a",
                                    count = 1
                                )
                            },
                            lang = lang
                        )
                    }

                    "tutor" -> {
                        val chatMessages by viewModel.chatMessages.collectAsState()
                        val isTutorThinking by viewModel.isTutorThinking.collectAsState()

                        AITutorScreen(
                            messages = chatMessages,
                            isTutorThinking = isTutorThinking,
                            userProfile = userProfile,
                            onSendMessage = { viewModel.sendChatMessage(it) },
                            onClearChat = { viewModel.clearChat() },
                            lang = lang
                        )
                    }

                    "summary" -> {
                        val currentSummary by viewModel.selectedSummary.collectAsState()
                        val isSummarizing by viewModel.isSummarizing.collectAsState()

                        SummaryScreen(
                            currentSummary = currentSummary,
                            isSummarizing = isSummarizing,
                            userProfile = userProfile,
                            onGenerateSummary = { title, subject, text, len ->
                                viewModel.generateSummary(title, subject, text, len)
                            },
                            onConvertToCards = { title, content ->
                                viewModel.createDeckFromContent(
                                    title = "Flashcards: $title",
                                    subject = userProfile?.major ?: "Academic",
                                    content = content,
                                    count = 5
                                )
                                viewModel.navigateTo("flashcards")
                            },
                            onConvertToQuiz = { subject, topic ->
                                viewModel.generateQuiz(subject, topic, 5, "Medium", "MCQ")
                                viewModel.navigateTo("quiz")
                            },
                            lang = lang
                        )
                    }

                    "flashcards" -> {
                        val decks by viewModel.allDecks.collectAsState()
                        val selectedDeck by viewModel.selectedDeck.collectAsState()
                        val isCreatingDeck by viewModel.isCreatingDeck.collectAsState()

                        FlashcardsScreen(
                            decks = decks,
                            selectedDeck = selectedDeck,
                            isCreatingDeck = isCreatingDeck,
                            onSelectDeck = { viewModel.selectDeck(it) },
                            onCreateDeck = { title, subject, content, count ->
                                viewModel.createDeckFromContent(title, subject, content, count)
                            },
                            onUpdateDeck = { id, json, mastered ->
                                viewModel.updateDeckMastery(id, json, mastered)
                            },
                            onDeleteDeck = { viewModel.deleteDeck(it) },
                            lang = lang
                        )
                    }

                    "quiz" -> {
                        val quizzes by viewModel.allQuizzes.collectAsState()
                        val activeQuiz by viewModel.activeQuiz.collectAsState()
                        val isGeneratingQuiz by viewModel.isGeneratingQuiz.collectAsState()

                        QuizScreen(
                            quizzes = quizzes,
                            activeQuiz = activeQuiz,
                            isGeneratingQuiz = isGeneratingQuiz,
                            userProfile = userProfile,
                            onSelectQuiz = { viewModel.selectQuiz(it) },
                            onGenerateQuiz = { subject, topic, count, difficulty, type ->
                                viewModel.generateQuiz(subject, topic, count, difficulty, type)
                            },
                            onSubmitResult = { id, score, weak ->
                                viewModel.submitQuizResult(id, score, weak)
                            },
                            onDeleteQuiz = { viewModel.deleteQuiz(it) },
                            lang = lang
                        )
                    }

                    "lectures" -> {
                        val lectures by viewModel.allLectures.collectAsState()
                        val isRecording by viewModel.isRecording.collectAsState()
                        val isPaused by viewModel.isRecordingPaused.collectAsState()
                        val duration by viewModel.recordingDuration.collectAsState()
                        val isAnalyzing by viewModel.isAnalyzingLecture.collectAsState()

                        LectureRecorderScreen(
                            lectures = lectures,
                            isRecording = isRecording,
                            isPaused = isPaused,
                            durationSeconds = duration,
                            isAnalyzing = isAnalyzing,
                            userProfile = userProfile,
                            onStartRecording = { viewModel.startLectureRecording() },
                            onPauseRecording = { viewModel.pauseLectureRecording() },
                            onResumeRecording = { viewModel.resumeLectureRecording() },
                            onStopRecording = { title, subject ->
                                viewModel.stopAndAnalyzeLecture(title, subject)
                            },
                            onDeleteLecture = { viewModel.deleteLecture(it) },
                            lang = lang
                        )
                    }

                    "library" -> {
                        val libraryItems by viewModel.allLibraryItems.collectAsState()

                        LibraryScreen(
                            libraryItems = libraryItems,
                            onAddItem = { name, type, size, summary ->
                                viewModel.addLibraryItem(name, type, size, summary)
                            },
                            onDeleteItem = { viewModel.deleteLibraryItem(it) },
                            onAnalyzeItem = { item ->
                                viewModel.generateSummary(item.name, userProfile?.major ?: "Academic", item.contentSummary, "standard")
                                viewModel.navigateTo("summary")
                            },
                            lang = lang
                        )
                    }

                    "history" -> {
                        val solutions by viewModel.allSolutions.collectAsState()
                        val summaries by viewModel.allSummaries.collectAsState()
                        val quizzes by viewModel.allQuizzes.collectAsState()
                        val lectures by viewModel.allLectures.collectAsState()

                        HistoryScreen(
                            solutions = solutions,
                            summaries = summaries,
                            quizzes = quizzes,
                            lectures = lectures,
                            onSelectSolution = { sol ->
                                viewModel.solveProblem(sol.problemText, null)
                                viewModel.navigateTo("solver")
                            },
                            onSelectSummary = { sum ->
                                viewModel.selectSummary(sum)
                                viewModel.navigateTo("summary")
                            },
                            onSelectQuiz = { qz ->
                                viewModel.selectQuiz(qz)
                                viewModel.navigateTo("quiz")
                            },
                            onDeleteSolution = { viewModel.deleteSolution(it) },
                            onDeleteSummary = { viewModel.deleteSummary(it) },
                            onDeleteQuiz = { viewModel.deleteQuiz(it) },
                            lang = lang
                        )
                    }

                    "progress" -> {
                        ProgressScreen(
                            userProfile = userProfile,
                            onPracticeWeakArea = { topic ->
                                viewModel.generateQuiz(userProfile?.major ?: "Academic", topic, 5, "Medium", "MCQ")
                                viewModel.navigateTo("quiz")
                            },
                            onTakeQuiz = {
                                viewModel.navigateTo("quiz")
                            },
                            lang = lang
                        )
                    }

                    "settings" -> {
                        val aiVerificationState by viewModel.aiVerificationState.collectAsState()
                        SettingsScreen(
                            userProfile = userProfile,
                            onLanguageChange = { viewModel.setLanguage(it) },
                            onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                            onUpgradeToPro = { viewModel.upgradeToPro() },
                            onEditPreferences = { viewModel.navigateTo("onboarding") },
                            lang = lang,
                            aiVerificationState = aiVerificationState,
                            onRunVerification = { prompt -> viewModel.runAIVerification(prompt) },
                            onResetVerification = { viewModel.resetAIVerification() }
                        )
                    }

                    "admin" -> {
                        val aiVerificationState by viewModel.aiVerificationState.collectAsState()
                        AdminScreen(
                            lang = lang,
                            aiVerificationState = aiVerificationState,
                            onRunVerification = { prompt -> viewModel.runAIVerification(prompt) },
                            onResetVerification = { viewModel.resetAIVerification() }
                        )
                    }

                    else -> {
                        LandingScreen(
                            onStartLearning = { viewModel.navigateTo("auth") },
                            onTryTutor = { viewModel.navigateTo("tutor") },
                            onExploreFeature = { viewModel.navigateTo("dashboard") },
                            lang = lang
                        )
                    }
                }
            }
        }
    }
}
