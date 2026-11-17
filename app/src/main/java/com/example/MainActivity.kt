package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ai.AiCurriculumService
import com.example.auth.AuthScreen
import com.example.auth.authStateFlow
import com.example.auth.signOut
import com.example.data.LearningRepository
import com.example.data.LessonRecord
import com.example.data.LocalSettingsRepository
import com.example.firebase.configureAppCheck
import com.example.ui.components.CyberBottomNavBar
import com.example.ui.components.CyberNavTab
import com.example.ui.components.CyberVoidBackground
import com.example.ui.screens.CurriculumScreen
import com.example.ui.screens.DailyScreen
import com.example.ui.screens.LessonViewerOverlay
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SandboxScreen
import com.example.ui.theme.DailyAiMasteryTheme
import com.example.ui.viewmodel.MainViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

private const val ROUTE_DAILY = "daily"
private const val ROUTE_CURRICULUM = "curriculum"
private const val ROUTE_SANDBOX = "sandbox"
private const val ROUTE_PROFILE = "profile"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent before UI or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            DailyAiMasteryTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        key(user.uid) {
            AuthenticatedAppShell(
                currentUserId = user.uid,
                displayName = user.displayName.orEmpty(),
                email = user.email
            )
        }
    }
}

@Composable
private fun AuthenticatedAppShell(
    currentUserId: String,
    displayName: String,
    email: String?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: MainViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                MainViewModel(
                    repository = LearningRepository(db),
                    localSettings = LocalSettingsRepository(app.applicationContext),
                    aiService = AiCurriculumService(),
                    currentUserId = currentUserId,
                    initialDisplayName = displayName
                )
            }
        }
    )

    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val deepDiveLoadingDay by viewModel.deepDiveLoadingDay.collectAsStateWithLifecycle()
    val deepDiveStreamText by viewModel.deepDiveStreamText.collectAsStateWithLifecycle()
    val deepDiveError by viewModel.deepDiveError.collectAsStateWithLifecycle()
    val sandboxState by viewModel.sandboxState.collectAsStateWithLifecycle()
    val probeState by viewModel.probeState.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableStateOf(ROUTE_DAILY) }
    var inspectedDayNumber by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(actionError) {
        if (!actionError.isNullOrBlank()) {
            Toast.makeText(context, actionError, Toast.LENGTH_LONG).show()
            viewModel.clearActionError()
        }
    }

    val tabs = listOf(
        CyberNavTab(
            route = ROUTE_DAILY,
            label = stringResource(R.string.tab_daily),
            icon = Icons.Rounded.Bolt,
            testTag = "nav_tab_daily"
        ),
        CyberNavTab(
            route = ROUTE_CURRICULUM,
            label = stringResource(R.string.tab_curriculum),
            icon = Icons.Rounded.AutoStories,
            testTag = "nav_tab_curriculum"
        ),
        CyberNavTab(
            route = ROUTE_SANDBOX,
            label = stringResource(R.string.tab_sandbox),
            icon = Icons.Rounded.Terminal,
            testTag = "nav_tab_sandbox"
        ),
        CyberNavTab(
            route = ROUTE_PROFILE,
            label = stringResource(R.string.tab_profile),
            icon = Icons.Rounded.Person,
            testTag = "nav_tab_profile"
        )
    )

    val inspectedLesson: LessonRecord? = inspectedDayNumber?.let { day ->
        dashboardState.lessons.firstOrNull { it.dayNumber == day }
    }

    CyberVoidBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (inspectedLesson == null) {
                    CyberBottomNavBar(
                        tabs = tabs,
                        selectedRoute = selectedTab,
                        onSelect = { selectedTab = it }
                    )
                }
            }
        ) { _ ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    ROUTE_DAILY -> DailyScreen(
                        dashboardState = dashboardState,
                        deepDiveLoadingDay = deepDiveLoadingDay,
                        deepDiveStreamText = deepDiveStreamText,
                        deepDiveError = deepDiveError,
                        onRetryInit = { viewModel.initializeUserData() },
                        onRetryGeneration = { viewModel.retryPrepareCurrentLesson() },
                        onGenerateDeepDive = { lesson ->
                            viewModel.clearDeepDiveError()
                            viewModel.generateDeepDive(lesson)
                        },
                        onSubmitYesterdayQuiz = { targetDay, isCorrect ->
                            viewModel.submitYesterdayQuiz(targetDay, isCorrect)
                        },
                        onCompleteLesson = { lesson, score, correct, total ->
                            viewModel.completeLesson(lesson, score, correct, total)
                        },
                        onOpenPastLesson = { lesson ->
                            inspectedDayNumber = lesson.dayNumber
                        }
                    )

                    ROUTE_CURRICULUM -> CurriculumScreen(
                        progress = dashboardState.progress,
                        lessons = dashboardState.lessons,
                        onOpenLesson = { lesson ->
                            inspectedDayNumber = lesson.dayNumber
                        }
                    )

                    ROUTE_SANDBOX -> SandboxScreen(
                        settings = dashboardState.settings,
                        sandboxState = sandboxState,
                        probeState = probeState,
                        onRunPrompt = { prompt -> viewModel.runSandboxPrompt(prompt) },
                        onTestConnection = { viewModel.testAiConnection() }
                    )

                    ROUTE_PROFILE -> ProfileScreen(
                        displayName = dashboardState.progress?.displayName ?: displayName,
                        email = email,
                        progress = dashboardState.progress,
                        settings = dashboardState.settings,
                        onSaveProxySettings = { key, url, model ->
                            viewModel.saveProxySettings(key, url, model)
                        },
                        onResetProxySettings = { viewModel.resetProxySettings() },
                        onToggleSelfPaced = { enabled -> viewModel.setSelfPacedMode(enabled) },
                        onSignOut = {
                            signOut(credentialManager, scope)
                        }
                    )
                }

                if (inspectedLesson != null) {
                    LessonViewerOverlay(
                        lesson = inspectedLesson,
                        isDeepDiveLoading = deepDiveLoadingDay == inspectedLesson.dayNumber,
                        streamingDeepDiveText = deepDiveStreamText
                            ?.takeIf { it.first == inspectedLesson.dayNumber }
                            ?.second,
                        deepDiveError = deepDiveError,
                        onGenerateDeepDive = {
                            viewModel.clearDeepDiveError()
                            viewModel.generateDeepDive(inspectedLesson)
                        },
                        onClose = { inspectedDayNumber = null }
                    )
                }
            }
        }
    }
}
