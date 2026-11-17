package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiCurriculumService
import com.example.ai.ProxyProbeResult
import com.example.ai.SandboxResult
import com.example.data.LearningRepository
import com.example.data.LessonRecord
import com.example.data.LocalSettingsRepository
import com.example.data.PrebuiltCurriculum
import com.example.data.ProxySettings
import com.example.data.UiState
import com.example.data.UserProgress
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

sealed interface DailyPhase {
    data object Loading : DailyPhase
    data class Error(val message: String) : DailyPhase
    data class LockedCountdown(
        val nextDay: Long,
        val remainingMs: Long,
        val lastLesson: LessonRecord?
    ) : DailyPhase
    data class YesterdayQuizGate(
        val targetDay: Long,
        val previousLesson: LessonRecord
    ) : DailyPhase
    data class GeneratingLesson(
        val dayNumber: Long,
        val isWeeklyAssessment: Boolean
    ) : DailyPhase
    data class LessonReady(
        val lesson: LessonRecord
    ) : DailyPhase
}

data class DailyDashboardState(
    val progress: UserProgress? = null,
    val lessons: List<LessonRecord> = emptyList(),
    val settings: ProxySettings = ProxySettings(),
    val phase: DailyPhase = DailyPhase.Loading
)

class MainViewModel(
    private val repository: LearningRepository,
    private val localSettings: LocalSettingsRepository,
    private val aiService: AiCurriculumService,
    private val currentUserId: String,
    private val initialDisplayName: String
) : ViewModel() {

    private val _initError = MutableStateFlow<String?>(null)
    private val _generationError = MutableStateFlow<String?>(null)
    private val _isGeneratingDay = MutableStateFlow<Long?>(null)

    private val _deepDiveLoadingDay = MutableStateFlow<Long?>(null)
    val deepDiveLoadingDay: StateFlow<Long?> = _deepDiveLoadingDay.asStateFlow()

    private val _deepDiveStreamText = MutableStateFlow<Pair<Long, String>?>(null)
    val deepDiveStreamText: StateFlow<Pair<Long, String>?> = _deepDiveStreamText.asStateFlow()

    private val _deepDiveError = MutableStateFlow<String?>(null)
    val deepDiveError: StateFlow<String?> = _deepDiveError.asStateFlow()

    private val _sandboxState = MutableStateFlow<UiState<SandboxResult>?>(null)
    val sandboxState: StateFlow<UiState<SandboxResult>?> = _sandboxState.asStateFlow()

    private val _probeState = MutableStateFlow<UiState<ProxyProbeResult>?>(null)
    val probeState: StateFlow<UiState<ProxyProbeResult>?> = _probeState.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    private val nowMsFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000L)
        }
    }

    private val progressState: StateFlow<UiState<UserProgress?>> =
        repository.observeUserProgress(currentUserId)
            .map<UserProgress?, UiState<UserProgress?>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, "Error observing user progress", root)
                emit(UiState.Error(root.message ?: "Ошибка загрузки прогресса"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    private val lessonsState: StateFlow<UiState<List<LessonRecord>>> =
        repository.observeLessons(currentUserId)
            .map<List<LessonRecord>, UiState<List<LessonRecord>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, "Error observing lessons", root)
                emit(UiState.Error(root.message ?: "Ошибка загрузки уроков"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    val settingsState: StateFlow<ProxySettings> = localSettings.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ProxySettings()
        )

    private data class CoreInputs(
        val prog: UiState<UserProgress?>,
        val lessons: UiState<List<LessonRecord>>,
        val settings: ProxySettings,
        val nowMs: Long
    )

    private val coreInputsFlow = combine(
        progressState,
        lessonsState,
        settingsState,
        nowMsFlow
    ) { prog, lessons, settings, nowMs ->
        CoreInputs(prog, lessons, settings, nowMs)
    }

    val dashboardState: StateFlow<DailyDashboardState> = combine(
        coreInputsFlow,
        _initError,
        _generationError,
        _isGeneratingDay
    ) { core, initErr, genErr, generatingDay ->
        if (initErr != null) {
            return@combine DailyDashboardState(
                settings = core.settings,
                phase = DailyPhase.Error(initErr)
            )
        }
        val progState = core.prog
        val lesState = core.lessons
        if (progState is UiState.Error) {
            return@combine DailyDashboardState(
                settings = core.settings,
                phase = DailyPhase.Error(progState.message)
            )
        }
        if (lesState is UiState.Error) {
            return@combine DailyDashboardState(
                settings = core.settings,
                phase = DailyPhase.Error(lesState.message)
            )
        }
        if (progState !is UiState.Success || lesState !is UiState.Success) {
            return@combine DailyDashboardState(
                settings = core.settings,
                phase = DailyPhase.Loading
            )
        }

        val progress = progState.data
            ?: return@combine DailyDashboardState(
                settings = core.settings,
                phase = DailyPhase.Loading
            )
        val lessons = lesState.data
        val lessonsByDay = lessons.associateBy { it.dayNumber }
        val activeDay = progress.currentDay.coerceAtLeast(1L)

        val remainingMs = if (core.settings.selfPacedMode) {
            0L
        } else {
            (progress.nextUnlockEpochMs - core.nowMs).coerceAtLeast(0L)
        }

        val phase = when {
            remainingMs > 0L -> {
                DailyPhase.LockedCountdown(
                    nextDay = activeDay,
                    remainingMs = remainingMs,
                    lastLesson = lessonsByDay[activeDay - 1L]
                )
            }
            activeDay >= 2L &&
                activeDay % 7L != 0L &&
                progress.yesterdayQuizPassedForDay < activeDay &&
                lessonsByDay[activeDay - 1L] != null -> {
                DailyPhase.YesterdayQuizGate(
                    targetDay = activeDay,
                    previousLesson = lessonsByDay.getValue(activeDay - 1L)
                )
            }
            lessonsByDay[activeDay] != null -> {
                DailyPhase.LessonReady(lessonsByDay.getValue(activeDay))
            }
            genErr != null -> {
                DailyPhase.Error(genErr)
            }
            else -> {
                ensureDayLessonPrepared(activeDay, lessons, core.settings)
                DailyPhase.GeneratingLesson(
                    dayNumber = activeDay,
                    isWeeklyAssessment = (activeDay % 7L == 0L)
                )
            }
        }

        DailyDashboardState(
            progress = progress,
            lessons = lessons,
            settings = core.settings,
            phase = phase
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = DailyDashboardState()
    )

    init {
        initializeUserData()
    }

    fun initializeUserData() {
        viewModelScope.launch {
            _initError.value = null
            val res = repository.ensureInitialized(currentUserId, initialDisplayName)
            res.onFailure { e ->
                _initError.value = e.localizedMessage ?: "Не удалось инициализировать профиль."
            }
        }
    }

    fun retryPrepareCurrentLesson() {
        _generationError.value = null
        val state = dashboardState.value
        val progress = state.progress ?: return
        ensureDayLessonPrepared(progress.currentDay, state.lessons, state.settings, force = true)
    }

    private fun ensureDayLessonPrepared(
        dayNumber: Long,
        existingLessons: List<LessonRecord>,
        settings: ProxySettings,
        force: Boolean = false
    ) {
        if (!force && (_isGeneratingDay.value == dayNumber || _generationError.value != null)) {
            return
        }
        _isGeneratingDay.value = dayNumber
        _generationError.value = null

        viewModelScope.launch {
            try {
                val prebuilt = PrebuiltCurriculum.getPrebuiltLesson(dayNumber)
                val lessonToSave = when {
                    prebuilt != null -> prebuilt
                    dayNumber % 7L == 0L -> {
                        val pastSix = existingLessons
                            .filter { it.dayNumber in (dayNumber - 6L).coerceAtLeast(1L) until dayNumber }
                        aiService.generateWeeklyAssessment(dayNumber, pastSix, settings)
                    }
                    else -> {
                        val pastTopics = existingLessons.map { it.topic }
                        aiService.generateDynamicLesson(dayNumber, pastTopics, settings)
                    }
                }
                repository.saveLesson(currentUserId, lessonToSave)
                    .onFailure { e ->
                        _generationError.value = e.localizedMessage ?: "Ошибка сохранения урока."
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed preparing lesson for day $dayNumber", e)
                _generationError.value = AiCurriculumService.formatRegionalError(
                    e,
                    settings.isCustomRouteActive
                )
            } finally {
                if (_isGeneratingDay.value == dayNumber) {
                    _isGeneratingDay.value = null
                }
            }
        }
    }

    fun generateDeepDive(lesson: LessonRecord) {
        if (_deepDiveLoadingDay.value == lesson.dayNumber) return
        val settings = settingsState.value
        _deepDiveLoadingDay.value = lesson.dayNumber
        _deepDiveError.value = null
        _deepDiveStreamText.value = lesson.dayNumber to ""

        viewModelScope.launch {
            var finalText = ""
            try {
                aiService.generateDeepDiveStream(lesson, settings).collect { partial ->
                    finalText = partial
                    _deepDiveStreamText.value = lesson.dayNumber to partial
                }
                if (finalText.isNotBlank()) {
                    repository.saveDeepDive(currentUserId, lesson.dayNumber, finalText)
                        .onFailure { e ->
                            _actionError.value = e.localizedMessage ?: "Не удалось сохранить Deep Dive."
                        }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Deep Dive generation failed", e)
                _deepDiveError.value = AiCurriculumService.formatRegionalError(
                    e,
                    settings.isCustomRouteActive
                )
            } finally {
                _deepDiveLoadingDay.value = null
                _deepDiveStreamText.value = null
            }
        }
    }

    fun submitYesterdayQuiz(targetDay: Long, isCorrect: Boolean) {
        val progress = dashboardState.value.progress ?: return
        viewModelScope.launch {
            repository.recordYesterdayQuizAnswer(
                userId = currentUserId,
                targetDay = targetDay,
                isCorrect = isCorrect,
                currentProgress = progress
            ).onFailure { e ->
                _actionError.value = e.localizedMessage ?: "Не удалось сохранить ответ."
            }
        }
    }

    fun completeLesson(
        lesson: LessonRecord,
        quizScore: Long = 0L,
        quizCorrectCount: Long = 0L,
        quizTotalCount: Long = 0L
    ) {
        val progress = dashboardState.value.progress ?: return
        viewModelScope.launch {
            repository.completeDay(
                userId = currentUserId,
                lesson = lesson,
                currentProgress = progress,
                quizScore = quizScore,
                quizCorrectCount = quizCorrectCount,
                quizTotalCount = quizTotalCount
            ).onFailure { e ->
                _actionError.value = e.localizedMessage ?: "Не удалось завершить урок."
            }
        }
    }

    fun runSandboxPrompt(prompt: String) {
        val clean = prompt.trim()
        if (clean.isEmpty()) return
        val settings = settingsState.value
        _sandboxState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val res = aiService.runSandboxPrompt(clean, settings)
                _sandboxState.value = UiState.Success(res)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Sandbox run failed", e)
                _sandboxState.value = UiState.Error(
                    AiCurriculumService.formatRegionalError(e, settings.isCustomRouteActive)
                )
            }
        }
    }

    fun testAiConnection() {
        val settings = settingsState.value
        _probeState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val result = aiService.testConnection(settings)
                _probeState.value = UiState.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _probeState.value = UiState.Error(e.localizedMessage ?: "Ошибка проверки канала.")
            }
        }
    }

    fun saveProxySettings(apiKey: String, proxyUrl: String, modelName: String) {
        viewModelScope.launch {
            localSettings.saveProxySettings(apiKey, proxyUrl, modelName)
        }
    }

    fun resetProxySettings() {
        viewModelScope.launch {
            localSettings.resetProxySettings()
        }
    }

    fun setSelfPacedMode(enabled: Boolean) {
        viewModelScope.launch {
            localSettings.setSelfPacedMode(enabled)
        }
    }

    fun clearActionError() {
        _actionError.value = null
    }

    fun clearDeepDiveError() {
        _deepDiveError.value = null
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}
