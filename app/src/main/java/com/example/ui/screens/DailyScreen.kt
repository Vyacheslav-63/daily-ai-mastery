package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.LessonRecord
import com.example.data.ProxySettings
import com.example.data.UserProgress
import com.example.ui.components.CategoryPillBadge
import com.example.ui.components.CyberPrimaryButton
import com.example.ui.components.CyberSecondaryButton
import com.example.ui.components.CyberSkeletonLoader
import com.example.ui.components.GlassCard
import com.example.ui.components.MarkdownCodeArticle
import com.example.ui.theme.CodeSmTextStyle
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.ErrorContainerDark
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LabelCapsTextStyle
import com.example.ui.theme.MidSlate
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.PureWhite
import com.example.ui.theme.VoidSlate
import com.example.ui.viewmodel.DailyDashboardState
import com.example.ui.viewmodel.DailyPhase
import java.util.Locale

@Composable
fun DailyScreen(
    dashboardState: DailyDashboardState,
    deepDiveLoadingDay: Long?,
    deepDiveStreamText: Pair<Long, String>?,
    deepDiveError: String?,
    onRetryInit: () -> Unit,
    onRetryGeneration: () -> Unit,
    onGenerateDeepDive: (LessonRecord) -> Unit,
    onSubmitYesterdayQuiz: (targetDay: Long, isCorrect: Boolean) -> Unit,
    onCompleteLesson: (LessonRecord, Long, Long, Long) -> Unit,
    onOpenPastLesson: (LessonRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 110.dp
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DailyTelemetryHeader(
                    progress = dashboardState.progress,
                    settings = dashboardState.settings
                )

                when (val phase = dashboardState.phase) {
                    is DailyPhase.Loading -> {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = "СИНХРОНИЗАЦИЯ ТРЕКА…",
                                    style = LabelCapsTextStyle,
                                    color = NeonCyan
                                )
                                CyberSkeletonLoader(lineCount = 6)
                            }
                        }
                    }

                    is DailyPhase.Error -> {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            glowColor = ErrorCoral
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Сбой загрузки модуля",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = PureWhite
                                )
                                Text(
                                    text = phase.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ErrorCoral
                                )
                                CyberPrimaryButton(
                                    onClick = {
                                        if (dashboardState.progress == null) onRetryInit()
                                        else onRetryGeneration()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("retry_daily_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = null,
                                        tint = VoidSlate
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.daily_retry),
                                        fontWeight = FontWeight.Bold,
                                        color = VoidSlate
                                    )
                                }
                            }
                        }
                    }

                    is DailyPhase.LockedCountdown -> {
                        CountdownLockCard(
                            phase = phase,
                            onReviewLast = {
                                phase.lastLesson?.let(onOpenPastLesson)
                            }
                        )
                    }

                    is DailyPhase.YesterdayQuizGate -> {
                        YesterdayQuizGateCard(
                            targetDay = phase.targetDay,
                            previousLesson = phase.previousLesson,
                            onAnswered = { isCorrect ->
                                onSubmitYesterdayQuiz(phase.targetDay, isCorrect)
                            }
                        )
                    }

                    is DailyPhase.GeneratingLesson -> {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            glowColor = CyberViolet
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = NeonCyan,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = if (phase.isWeeklyAssessment) {
                                            "GEMINI ГЕНЕРИРУЕТ СРЕЗ ДНЯ ${phase.dayNumber}…"
                                        } else {
                                            "GEMINI СОЗДАЁТ УРОК ДНЯ ${phase.dayNumber}…"
                                        },
                                        style = LabelCapsTextStyle,
                                        color = CyberPurple
                                    )
                                }
                                Text(
                                    text = if (phase.isWeeklyAssessment) {
                                        "Анализ пройденных тем за 6 дней и сборка сводного теста в JSON."
                                    } else {
                                        "Адаптация программы под ваш прогресс и генерация примеров кода."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MidSlate
                                )
                                CyberSkeletonLoader(lineCount = 7)
                            }
                        }
                    }

                    is DailyPhase.LessonReady -> {
                        val lesson = phase.lesson
                        if (lesson.weeklyAssessment) {
                            WeeklyAssessmentSection(
                                lesson = lesson,
                                onComplete = { score, total ->
                                    onCompleteLesson(lesson, score, score, total)
                                }
                            )
                        } else {
                            val activeStream = deepDiveStreamText
                                ?.takeIf { it.first == lesson.dayNumber }
                                ?.second
                            LessonContentSection(
                                lesson = lesson,
                                isDeepDiveLoading = deepDiveLoadingDay == lesson.dayNumber,
                                streamingDeepDiveText = activeStream,
                                deepDiveError = deepDiveError,
                                onGenerateDeepDive = { onGenerateDeepDive(lesson) },
                                onCompleteLesson = {
                                    onCompleteLesson(lesson, 0L, 0L, 0L)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyTelemetryHeader(
    progress: UserProgress?,
    settings: ProxySettings
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (settings.isCustomRouteActive) CyberViolet else NeonCyan)
                    )
                    Text(
                        text = if (settings.isCustomRouteActive) "CUSTOM PROXY ROUTE" else "FIREBASE AI • SECURE",
                        style = LabelCapsTextStyle,
                        color = if (settings.isCustomRouteActive) CyberPurple else ElectricTeal
                    )
                }
                Text(
                    text = "День ${progress?.currentDay ?: 1}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = PureWhite
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TelemetryPill(
                    icon = Icons.Rounded.Bolt,
                    value = "${progress?.streakDays ?: 0} дн",
                    tint = NeonAmber
                )
                TelemetryPill(
                    icon = Icons.Rounded.AutoAwesome,
                    value = "${progress?.totalXp ?: 0} XP",
                    tint = NeonCyan
                )
            }
        }
    }
}

@Composable
private fun TelemetryPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    tint: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(tint.copy(alpha = 0.12f))
            .border(1.dp, tint.copy(alpha = 0.35f), RoundedCornerShape(9999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = value,
            style = CodeSmTextStyle,
            color = PureWhite
        )
    }
}

@Composable
private fun CountdownLockCard(
    phase: DailyPhase.LockedCountdown,
    onReviewLast: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = CyberViolet
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(CyberViolet.copy(alpha = 0.16f))
                    .border(1.dp, CyberViolet.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = CyberPurple,
                    modifier = Modifier.size(26.dp)
                )
            }
            Text(
                text = "ДЕНЬ ${phase.nextDay} ОТКРОЕТСЯ ЧЕРЕЗ",
                style = LabelCapsTextStyle,
                color = CyberPurple
            )
            Text(
                text = formatCountdown(phase.remainingMs),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    color = NeonCyan
                )
            )
            Text(
                text = stringResource(R.string.daily_locked_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MidSlate
            )
            if (phase.lastLesson != null) {
                CyberSecondaryButton(
                    onClick = onReviewLast,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_last_lesson_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Повторить День ${phase.lastLesson.dayNumber}: ${phase.lastLesson.topic}",
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun YesterdayQuizGateCard(
    targetDay: Long,
    previousLesson: LessonRecord,
    onAnswered: (Boolean) -> Unit
) {
    var selectedIdx by remember(previousLesson.dayNumber) { mutableIntStateOf(-1) }
    var submitted by remember(previousLesson.dayNumber) { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = NeonCyan
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CategoryPillBadge(
                text = stringResource(R.string.daily_gate_badge, previousLesson.dayNumber),
                isAccentViolet = false
            )
            Text(
                text = stringResource(R.string.daily_gate_title),
                style = MaterialTheme.typography.headlineMedium,
                color = PureWhite
            )
            Text(
                text = stringResource(
                    R.string.daily_gate_subtitle,
                    previousLesson.topic,
                    targetDay
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MidSlate
            )
            HorizontalDivider(color = GlassBorder)
            Text(
                text = previousLesson.question,
                style = MaterialTheme.typography.titleMedium,
                color = PureWhite
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                previousLesson.options.forEachIndexed { idx, optionText ->
                    val isSelected = selectedIdx == idx
                    val isCorrectOption = idx.toLong() == previousLesson.correctAnswerIndex
                    val borderColor = when {
                        submitted && isCorrectOption -> NeonEmerald
                        submitted && isSelected && !isCorrectOption -> ErrorCoral
                        isSelected -> NeonCyan
                        else -> GlassBorder
                    }
                    val bgColor = when {
                        submitted && isCorrectOption -> NeonEmerald.copy(alpha = 0.14f)
                        submitted && isSelected && !isCorrectOption -> ErrorContainerDark.copy(alpha = 0.45f)
                        isSelected -> NeonCyan.copy(alpha = 0.12f)
                        else -> Color.White.copy(alpha = 0.03f)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            .minimumInteractiveComponentSize()
                            .clickable(enabled = !submitted) { selectedIdx = idx }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("gate_option_$idx"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(borderColor.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ('A' + idx).toString(),
                                style = CodeSmTextStyle,
                                color = PureWhite
                            )
                        }
                        Text(
                            text = optionText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PureWhite
                        )
                    }
                }
            }

            if (submitted) {
                val wasCorrect = selectedIdx.toLong() == previousLesson.correctAnswerIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (wasCorrect) NeonEmerald.copy(alpha = 0.12f)
                            else CyberViolet.copy(alpha = 0.14f)
                        )
                        .border(
                            1.dp,
                            if (wasCorrect) NeonEmerald.copy(alpha = 0.45f) else CyberViolet.copy(alpha = 0.45f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (wasCorrect) "Верно! +25 XP" else "Зафиксировано (+10 XP)",
                            style = CodeSmTextStyle,
                            color = if (wasCorrect) NeonEmerald else CyberPurple
                        )
                        if (previousLesson.explanation.isNotBlank()) {
                            Text(
                                text = previousLesson.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = PureWhite
                            )
                        }
                    }
                }
            }

            CyberPrimaryButton(
                onClick = {
                    if (!submitted) {
                        submitted = true
                    } else {
                        val wasCorrect = selectedIdx.toLong() == previousLesson.correctAnswerIndex
                        onAnswered(wasCorrect)
                    }
                },
                enabled = selectedIdx >= 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gate_submit_button")
            ) {
                Text(
                    text = if (!submitted) {
                        "Проверить ответ"
                    } else {
                        stringResource(R.string.daily_gate_continue, targetDay)
                    },
                    fontWeight = FontWeight.Bold,
                    color = VoidSlate
                )
            }
        }
    }
}

@Composable
fun LessonContentSection(
    lesson: LessonRecord,
    isDeepDiveLoading: Boolean,
    streamingDeepDiveText: String?,
    deepDiveError: String?,
    onGenerateDeepDive: () -> Unit,
    onCompleteLesson: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val effectiveDeepDive = streamingDeepDiveText?.takeIf { it.isNotBlank() }
        ?: lesson.deepDiveContent.takeIf { it.isNotBlank() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CategoryPillBadge(text = "ДЕНЬ ${lesson.dayNumber}")
                        CategoryPillBadge(text = lesson.category, isAccentViolet = true)
                    }
                    Text(
                        text = "${lesson.estimatedMinutes} MIN • +${lesson.xpReward} XP",
                        style = CodeSmTextStyle,
                        color = MidSlate
                    )
                }

                Text(
                    text = lesson.topic,
                    style = MaterialTheme.typography.headlineLarge,
                    color = PureWhite
                )

                if (lesson.summary.isNotBlank()) {
                    Text(
                        text = lesson.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MidSlate
                    )
                }

                HorizontalDivider(color = GlassBorder)

                MarkdownCodeArticle(markdown = lesson.content)
            }
        }

        // Deep Dive Action Button
        if (effectiveDeepDive == null && !isDeepDiveLoading) {
            CyberSecondaryButton(
                onClick = onGenerateDeepDive,
                accent = CyberViolet,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("deep_dive_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Psychology,
                    contentDescription = null,
                    tint = CyberPurple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.daily_deep_dive_cta),
                    fontWeight = FontWeight.SemiBold,
                    color = PureWhite
                )
            }
        }

        if (deepDiveError != null) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = ErrorCoral
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = deepDiveError,
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorCoral
                    )
                    CyberSecondaryButton(
                        onClick = onGenerateDeepDive,
                        accent = NeonCyan,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Повторить Deep Dive")
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isDeepDiveLoading || effectiveDeepDive != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("deep_dive_card"),
                glowColor = CyberViolet
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = CyberPurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isDeepDiveLoading) {
                                stringResource(R.string.daily_deep_dive_generating)
                            } else {
                                stringResource(R.string.daily_deep_dive_title)
                            },
                            style = LabelCapsTextStyle,
                            color = CyberPurple
                        )
                    }

                    if (isDeepDiveLoading && effectiveDeepDive.isNullOrBlank()) {
                        CyberSkeletonLoader(lineCount = 6)
                    } else if (!effectiveDeepDive.isNullOrBlank()) {
                        MarkdownCodeArticle(markdown = effectiveDeepDive)
                    }
                }
            }
        }

        if (onCompleteLesson != null) {
            if (lesson.completed) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = NeonEmerald
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = NeonEmerald
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.daily_lesson_completed),
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonEmerald
                        )
                    }
                }
            } else {
                CyberPrimaryButton(
                    onClick = onCompleteLesson,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("complete_lesson_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = VoidSlate,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.daily_complete_lesson, lesson.xpReward),
                        fontWeight = FontWeight.Bold,
                        color = VoidSlate
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyAssessmentSection(
    lesson: LessonRecord,
    onComplete: (correctCount: Long, totalCount: Long) -> Unit
) {
    val selectedAnswers = remember(lesson.dayNumber) { mutableStateMapOf<Int, Int>() }
    var submitted by remember(lesson.dayNumber) { mutableStateOf(lesson.completed) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            glowColor = CyberViolet
        ) {
            Column {
                Image(
                    painter = painterResource(id = R.drawable.img_weekly_sprint_1794933160468),
                    contentDescription = stringResource(R.string.cd_weekly_banner),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp)
                )
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryPillBadge(
                        text = stringResource(R.string.daily_weekly_badge, lesson.dayNumber),
                        isAccentViolet = true
                    )
                    Text(
                        text = lesson.topic,
                        style = MaterialTheme.typography.headlineMedium,
                        color = PureWhite
                    )
                    Text(
                        text = lesson.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MidSlate
                    )
                }
            }
        }

        lesson.weeklyQuestions.forEachIndexed { qIdx, q ->
            val picked = selectedAnswers[qIdx]
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryPillBadge(text = q.topicTag.ifBlank { "ВОПРОС ${qIdx + 1}" })
                        Text(
                            text = "${qIdx + 1}/${lesson.weeklyQuestions.size}",
                            style = CodeSmTextStyle,
                            color = MidSlate
                        )
                    }
                    Text(
                        text = q.question,
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite
                    )
                    q.options.forEachIndexed { optIdx, optText ->
                        val isSelected = picked == optIdx
                        val isCorrect = optIdx.toLong() == q.correctAnswerIndex
                        val border = when {
                            submitted && isCorrect -> NeonEmerald
                            submitted && isSelected && !isCorrect -> ErrorCoral
                            isSelected -> NeonCyan
                            else -> GlassBorder
                        }
                        val bg = when {
                            submitted && isCorrect -> NeonEmerald.copy(alpha = 0.14f)
                            submitted && isSelected && !isCorrect -> ErrorContainerDark.copy(alpha = 0.45f)
                            isSelected -> NeonCyan.copy(alpha = 0.12f)
                            else -> Color.White.copy(alpha = 0.03f)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(bg)
                                .border(1.dp, border, RoundedCornerShape(10.dp))
                                .minimumInteractiveComponentSize()
                                .clickable(enabled = !submitted) { selectedAnswers[qIdx] = optIdx }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${('A' + optIdx)}.",
                                style = CodeSmTextStyle,
                                color = if (isSelected) NeonCyan else MidSlate
                            )
                            Text(
                                text = optText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = PureWhite
                            )
                        }
                    }
                    if (submitted && q.explanation.isNotBlank()) {
                        Text(
                            text = q.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricTeal
                        )
                    }
                }
            }
        }

        val allAnswered = selectedAnswers.size == lesson.weeklyQuestions.size
        if (!lesson.completed) {
            CyberPrimaryButton(
                onClick = {
                    if (!submitted) {
                        submitted = true
                    } else {
                        val correctCount = lesson.weeklyQuestions.indices.count { idx ->
                            selectedAnswers[idx]?.toLong() == lesson.weeklyQuestions[idx].correctAnswerIndex
                        }.toLong()
                        onComplete(correctCount, lesson.weeklyQuestions.size.toLong())
                    }
                },
                enabled = allAnswered && lesson.weeklyQuestions.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_weekly_assessment_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = VoidSlate,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (!submitted) {
                        "Проверить результаты"
                    } else {
                        stringResource(R.string.daily_weekly_submit)
                    },
                    fontWeight = FontWeight.Bold,
                    color = VoidSlate
                )
            }
        }
    }
}

private fun formatCountdown(remainingMs: Long): String {
    val totalSec = (remainingMs / 1000L).coerceAtLeast(0L)
    val hours = totalSec / 3600L
    val minutes = (totalSec % 3600L) / 60L
    val seconds = totalSec % 60L
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}
