package com.example.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.LessonRecord
import com.example.data.PrebuiltCurriculum
import com.example.data.UserProgress
import com.example.ui.components.CategoryPillBadge
import com.example.ui.components.GlassCard
import com.example.ui.theme.CodeSmTextStyle
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.LabelCapsTextStyle
import com.example.ui.theme.LowDimSlate
import com.example.ui.theme.MidSlate
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceContainerHigh

private data class RoadmapNode(
    val dayNumber: Long,
    val title: String,
    val category: String,
    val isDynamic: Boolean,
    val isWeekly: Boolean,
    val lesson: LessonRecord?
)

@Composable
fun CurriculumScreen(
    progress: UserProgress?,
    lessons: List<LessonRecord>,
    onOpenLesson: (LessonRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    val byDay = lessons.associateBy { it.dayNumber }
    val currentDay = progress?.currentDay ?: 1L
    val maxDayToShow = maxOf(14L, currentDay + 3L)

    val roadmap = (1L..maxDayToShow).map { day ->
        val saved = byDay[day]
        val prebuilt = PrebuiltCurriculum.getPrebuiltLesson(day)
        when {
            saved != null -> RoadmapNode(
                dayNumber = day,
                title = saved.topic,
                category = saved.category,
                isDynamic = day >= 6L,
                isWeekly = saved.weeklyAssessment,
                lesson = saved
            )
            prebuilt != null -> RoadmapNode(
                dayNumber = day,
                title = prebuilt.topic,
                category = prebuilt.category,
                isDynamic = false,
                isWeekly = false,
                lesson = null
            )
            day % 7L == 0L -> RoadmapNode(
                dayNumber = day,
                title = "Еженедельный срез знаний (Дни ${day - 6}–${day - 1})",
                category = "WEEKLY SPRINT",
                isDynamic = true,
                isWeekly = true,
                lesson = null
            )
            else -> RoadmapNode(
                dayNumber = day,
                title = "Динамический AI-модуль (Gemini JSON)",
                category = "GEMINI TRACK",
                isDynamic = true,
                isWeekly = false,
                lesson = null
            )
        }
    }

    val completedCount = progress?.highestCompletedDay ?: 0L
    val cycleProgress = ((completedCount % 7L).toFloat() / 7f).coerceIn(0f, 1f)

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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = NeonCyan
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "КАРТА МАСТЕРСТВА ИИ",
                            style = LabelCapsTextStyle,
                            color = NeonCyan
                        )
                        Text(
                            text = "Программа обучения",
                            style = MaterialTheme.typography.headlineMedium,
                            color = PureWhite
                        )
                        Text(
                            text = "Дни 1–5 — фундамент (сети, прокси, VPN, безопасность, MCP). С 6-го дня трек генерируется Gemini, а каждый 7-й день — срез знаний.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MidSlate
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Пройдено: $completedCount дн.",
                                style = CodeSmTextStyle,
                                color = PureWhite
                            )
                            Text(
                                text = "До среза: ${7 - (completedCount % 7)} дн.",
                                style = CodeSmTextStyle,
                                color = CyberPurple
                            )
                        }
                        LinearProgressIndicator(
                            progress = { if (completedCount > 0 && completedCount % 7L == 0L) 1f else cycleProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(9999.dp)),
                            color = NeonCyan,
                            trackColor = SurfaceContainerHigh
                        )
                    }
                }
            }
        }

        items(roadmap, key = { it.dayNumber }) { node ->
            val isCompleted = node.lesson?.completed == true
            val isUnlocked = node.lesson != null
            val isCurrent = node.dayNumber == currentDay

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp)
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .clickable(enabled = isUnlocked) {
                            node.lesson?.let(onOpenLesson)
                        }
                        .testTag("curriculum_day_${node.dayNumber}"),
                    glowColor = when {
                        isCurrent -> NeonCyan
                        node.isWeekly && isUnlocked -> CyberViolet
                        isCompleted -> NeonEmerald.copy(alpha = 0.65f)
                        else -> null
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCompleted -> NeonEmerald.copy(alpha = 0.16f)
                                        isCurrent -> NeonCyan.copy(alpha = 0.16f)
                                        node.isWeekly -> CyberViolet.copy(alpha = 0.14f)
                                        else -> Color.White.copy(alpha = 0.05f)
                                    }
                                )
                                .border(
                                    1.dp,
                                    when {
                                        isCompleted -> NeonEmerald
                                        isCurrent -> NeonCyan
                                        node.isWeekly -> CyberViolet.copy(alpha = 0.5f)
                                        else -> LowDimSlate.copy(alpha = 0.4f)
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    isCompleted -> Icons.Rounded.CheckCircle
                                    isUnlocked -> Icons.Rounded.PlayArrow
                                    node.isDynamic -> Icons.Rounded.AutoAwesome
                                    else -> Icons.Rounded.Lock
                                },
                                contentDescription = null,
                                tint = when {
                                    isCompleted -> NeonEmerald
                                    isCurrent -> NeonCyan
                                    isUnlocked -> PureWhite
                                    node.isWeekly -> CyberPurple
                                    else -> LowDimSlate
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "ДЕНЬ ${node.dayNumber}",
                                    style = LabelCapsTextStyle,
                                    color = if (isUnlocked) ElectricTeal else MidSlate
                                )
                                CategoryPillBadge(
                                    text = node.category,
                                    isAccentViolet = node.isDynamic || node.isWeekly
                                )
                            }
                            Text(
                                text = node.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isUnlocked) PureWhite else MidSlate
                            )
                            if (node.lesson != null && node.lesson.deepDiveContent.isNotBlank()) {
                                Text(
                                    text = "• Deep Dive сохранён",
                                    style = CodeSmTextStyle,
                                    color = CyberPurple
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
