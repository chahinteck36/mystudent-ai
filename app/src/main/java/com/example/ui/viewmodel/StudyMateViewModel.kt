package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.FlashcardDeckEntity
import com.example.data.local.entity.LectureNoteEntity
import com.example.data.local.entity.LessonSummaryEntity
import com.example.data.local.entity.LibraryItemEntity
import com.example.data.local.entity.ProblemSolutionEntity
import com.example.data.local.entity.QuizEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.repository.StudyMateRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudyMateViewModel(
    private val repository: StudyMateRepository
) : ViewModel() {

    // Current Navigation Screen
    private val _currentRoute = MutableStateFlow(
        if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null) "dashboard" else "landing"
    )
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()

    fun navigateTo(route: String) {
        _currentRoute.value = route
    }

    fun signOut() {
        try {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            // ignore
        }
        _currentRoute.value = "auth"
    }

    // User Profile
    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateAcademicPreferences(
        level: String,
        major: String,
        subjects: String,
        learningPreferences: String,
        mainGoal: String
    ) {
        viewModelScope.launch {
            repository.updateAcademicPreferences(level, major, subjects, learningPreferences, mainGoal)
            _currentRoute.value = "dashboard"
        }
    }

    fun updateUserAccount(name: String, email: String) {
        viewModelScope.launch {
            repository.updateUserAccount(name, email)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch { repository.setLanguage(lang) }
    }

    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch { repository.toggleDarkMode(isDark) }
    }

    fun upgradeToPro() {
        viewModelScope.launch { repository.upgradeToPro() }
    }

    // Problem Solver
    val allSolutions: StateFlow<List<ProblemSolutionEntity>> = repository.allSolutions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSolution = MutableStateFlow<ProblemSolutionEntity?>(null)
    val currentSolution: StateFlow<ProblemSolutionEntity?> = _currentSolution.asStateFlow()

    private val _isSolving = MutableStateFlow(false)
    val isSolving: StateFlow<Boolean> = _isSolving.asStateFlow()

    private val _solverError = MutableStateFlow<String?>(null)
    val solverError: StateFlow<String?> = _solverError.asStateFlow()

    fun solveProblem(text: String, imageBase64: String? = null) {
        if (text.isBlank() && imageBase64 == null) return
        _isSolving.value = true
        _solverError.value = null
        viewModelScope.launch {
            val result = repository.solveAndSaveProblem(text, imageBase64)
            _isSolving.value = false
            if (result.isSuccess) {
                _currentSolution.value = result.getOrNull()
            } else {
                _solverError.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to solve problem"
            }
        }
    }

    fun selectSolution(solution: ProblemSolutionEntity) {
        _currentSolution.value = solution
        _currentRoute.value = "solver"
    }

    fun deleteSolution(id: Long) {
        viewModelScope.launch { repository.deleteSolution(id) }
    }

    // AI Tutor Chat
    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isTutorThinking = MutableStateFlow(false)
    val isTutorThinking: StateFlow<Boolean> = _isTutorThinking.asStateFlow()

    fun sendChatMessage(message: String) {
        if (message.isBlank()) return
        _isTutorThinking.value = true
        viewModelScope.launch {
            repository.sendChatMessage(message)
            _isTutorThinking.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch { repository.clearChat() }
    }

    // Summaries
    val allSummaries: StateFlow<List<LessonSummaryEntity>> = repository.allSummaries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSummarizing = MutableStateFlow(false)
    val isSummarizing: StateFlow<Boolean> = _isSummarizing.asStateFlow()

    private val _summaryError = MutableStateFlow<String?>(null)
    val summaryError: StateFlow<String?> = _summaryError.asStateFlow()

    private val _selectedSummary = MutableStateFlow<LessonSummaryEntity?>(null)
    val selectedSummary: StateFlow<LessonSummaryEntity?> = _selectedSummary.asStateFlow()

    fun generateSummary(title: String, subject: String, text: String, length: String) {
        if (text.isBlank()) return
        _isSummarizing.value = true
        _summaryError.value = null
        viewModelScope.launch {
            val res = repository.generateAndSaveSummary(title, subject, text, length)
            _isSummarizing.value = false
            if (res.isSuccess) {
                _selectedSummary.value = res.getOrNull()
            } else {
                _summaryError.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to generate summary"
            }
        }
    }

    fun selectSummary(summary: LessonSummaryEntity) {
        _selectedSummary.value = summary
        _currentRoute.value = "summaries"
    }

    fun deleteSummary(id: Long) {
        viewModelScope.launch { repository.deleteSummary(id) }
    }

    // Flashcards
    val allDecks: StateFlow<List<FlashcardDeckEntity>> = repository.allDecks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDeck = MutableStateFlow<FlashcardDeckEntity?>(null)
    val selectedDeck: StateFlow<FlashcardDeckEntity?> = _selectedDeck.asStateFlow()

    private val _isCreatingDeck = MutableStateFlow(false)
    val isCreatingDeck: StateFlow<Boolean> = _isCreatingDeck.asStateFlow()

    private val _deckError = MutableStateFlow<String?>(null)
    val deckError: StateFlow<String?> = _deckError.asStateFlow()

    fun selectDeck(deck: FlashcardDeckEntity?) {
        _selectedDeck.value = deck
    }

    fun createDeckFromContent(title: String, subject: String, content: String, count: Int = 5) {
        if (title.isBlank() || content.isBlank()) return
        _isCreatingDeck.value = true
        _deckError.value = null
        viewModelScope.launch {
            val res = repository.createDeckFromAI(title, subject, content, count)
            _isCreatingDeck.value = false
            if (res.isSuccess) {
                _selectedDeck.value = res.getOrNull()
            } else {
                _deckError.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to create flashcards"
            }
        }
    }

    fun updateDeckMastery(deckId: Long, updatedJson: String, masteredCount: Int) {
        viewModelScope.launch {
            repository.updateDeckCards(deckId, updatedJson, masteredCount)
        }
    }

    fun deleteDeck(id: Long) {
        viewModelScope.launch { repository.deleteDeck(id) }
    }

    // Quizzes
    val allQuizzes: StateFlow<List<QuizEntity>> = repository.allQuizzes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeQuiz = MutableStateFlow<QuizEntity?>(null)
    val activeQuiz: StateFlow<QuizEntity?> = _activeQuiz.asStateFlow()

    private val _isGeneratingQuiz = MutableStateFlow(false)
    val isGeneratingQuiz: StateFlow<Boolean> = _isGeneratingQuiz.asStateFlow()

    private val _quizError = MutableStateFlow<String?>(null)
    val quizError: StateFlow<String?> = _quizError.asStateFlow()

    fun generateQuiz(subject: String, topic: String, count: Int, difficulty: String, type: String) {
        _isGeneratingQuiz.value = true
        _quizError.value = null
        viewModelScope.launch {
            val res = repository.generateQuiz(subject, topic, count, difficulty, type)
            _isGeneratingQuiz.value = false
            if (res.isSuccess) {
                _activeQuiz.value = res.getOrNull()
            } else {
                _quizError.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to generate quiz"
            }
        }
    }

    fun selectQuiz(quiz: QuizEntity?) {
        _activeQuiz.value = quiz
    }

    fun submitQuizResult(quizId: Long, score: Int, weakTopics: String) {
        viewModelScope.launch {
            repository.submitQuizResult(quizId, score, weakTopics)
        }
    }

    fun deleteQuiz(id: Long) {
        viewModelScope.launch { repository.deleteQuiz(id) }
    }

    // Lecture Recording State
    val allLectures: StateFlow<List<LectureNoteEntity>> = repository.allLectures
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isRecordingPaused = MutableStateFlow(false)
    val isRecordingPaused: StateFlow<Boolean> = _isRecordingPaused.asStateFlow()

    private val _recordingDuration = MutableStateFlow(0)
    val recordingDuration: StateFlow<Int> = _recordingDuration.asStateFlow()

    private val _isAnalyzingLecture = MutableStateFlow(false)
    val isAnalyzingLecture: StateFlow<Boolean> = _isAnalyzingLecture.asStateFlow()

    private val _lectureError = MutableStateFlow<String?>(null)
    val lectureError: StateFlow<String?> = _lectureError.asStateFlow()

    private var recordingTimerJob: Job? = null

    fun startLectureRecording() {
        _isRecording.value = true
        _isRecordingPaused.value = false
        _recordingDuration.value = 0
        _lectureError.value = null
        startTimer()
    }

    fun pauseLectureRecording() {
        _isRecordingPaused.value = true
        recordingTimerJob?.cancel()
    }

    fun resumeLectureRecording() {
        _isRecordingPaused.value = false
        startTimer()
    }

    private fun startTimer() {
        recordingTimerJob?.cancel()
        recordingTimerJob = viewModelScope.launch {
            while (_isRecording.value && !_isRecordingPaused.value) {
                delay(1000)
                _recordingDuration.value += 1
            }
        }
    }

    fun stopAndAnalyzeLecture(title: String, subject: String, transcript: String) {
        _isRecording.value = false
        _isRecordingPaused.value = false
        recordingTimerJob?.cancel()
        val duration = _recordingDuration.value
        _lectureError.value = null

        val cleanTranscript = transcript.trim()
        if (cleanTranscript.isBlank()) {
            _lectureError.value = "No audio or speech was transcribed. Please make sure the microphone is enabled and speak clearly."
            return
        }

        _isAnalyzingLecture.value = true
        viewModelScope.launch {
            val result = repository.processLectureRecording(
                title = if (title.isNotBlank()) title else "Lecture: $subject",
                subject = subject,
                durationSeconds = duration,
                transcript = cleanTranscript
            )
            _isAnalyzingLecture.value = false
            if (result.isFailure) {
                _lectureError.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to analyze lecture."
            }
        }
    }

    fun deleteLecture(id: Long) {
        viewModelScope.launch { repository.deleteLecture(id) }
    }

    // Library Items
    val allLibraryItems: StateFlow<List<LibraryItemEntity>> = repository.allLibraryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addLibraryItem(name: String, type: String, size: String, summary: String) {
        viewModelScope.launch {
            repository.addLibraryItem(name, type, size, summary)
        }
    }

    fun deleteLibraryItem(id: Long) {
        viewModelScope.launch { repository.deleteLibraryItem(id) }
    }

    // Firebase AI Logic Runtime Verification
    private val _aiVerificationState = MutableStateFlow<AIVerificationUiState>(AIVerificationUiState.Idle)
    val aiVerificationState: StateFlow<AIVerificationUiState> = _aiVerificationState.asStateFlow()

    fun runAIVerification(prompt: String = "Explain photosynthesis in one short paragraph.") {
        val testPrompt = prompt.ifBlank { "Explain photosynthesis in one short paragraph." }
        _aiVerificationState.value = AIVerificationUiState.Loading(testPrompt)
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val result = repository.verifyAILogic(testPrompt)
            val duration = System.currentTimeMillis() - startTime
            result.onSuccess { responseText ->
                _aiVerificationState.value = AIVerificationUiState.Success(
                    prompt = testPrompt,
                    response = responseText,
                    durationMs = duration
                )
            }.onFailure { error ->
                _aiVerificationState.value = AIVerificationUiState.Error(
                    prompt = testPrompt,
                    errorMessage = error.localizedMessage ?: error.message ?: "Failed to execute Firebase AI request",
                    durationMs = duration
                )
            }
        }
    }

    fun resetAIVerification() {
        _aiVerificationState.value = AIVerificationUiState.Idle
    }
}

sealed interface AIVerificationUiState {
    object Idle : AIVerificationUiState
    data class Loading(val prompt: String) : AIVerificationUiState
    data class Success(
        val prompt: String,
        val response: String,
        val durationMs: Long,
        val model: String = "gemini-2.5-flash",
        val backend: String = "GenerativeBackend.googleAI() (Gemini Developer API)"
    ) : AIVerificationUiState
    data class Error(
        val prompt: String,
        val errorMessage: String,
        val durationMs: Long
    ) : AIVerificationUiState
}

class StudyMateViewModelFactory(
    private val repository: StudyMateRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyMateViewModel::class.java)) {
            return StudyMateViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
