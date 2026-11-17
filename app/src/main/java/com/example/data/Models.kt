package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

@Keep
@IgnoreExtraProperties
data class UserProgress(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val currentDay: Long = 1L,
    val highestCompletedDay: Long = 0L,
    val streakDays: Long = 0L,
    val totalXp: Long = 0L,
    val correctAnswers: Long = 0L,
    val totalAnswers: Long = 0L,
    val nextUnlockEpochMs: Long = 0L,
    val yesterdayQuizPassedForDay: Long = 1L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WeeklyQuestion(
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Long = 0L,
    val topicTag: String = "",
    val explanation: String = ""
)

@Keep
@IgnoreExtraProperties
data class LessonRecord(
    @DocumentId val id: String = "",
    val dayNumber: Long = 1L,
    val topic: String = "",
    val category: String = "",
    val summary: String = "",
    val content: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Long = 0L,
    val explanation: String = "",
    val deepDiveContent: String = "",
    val weeklyAssessment: Boolean = false,
    val weeklyQuestions: List<WeeklyQuestion> = emptyList(),
    val xpReward: Long = 100L,
    val estimatedMinutes: Long = 5L,
    val completed: Boolean = false,
    val quizScore: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class ProxySettings(
    val customApiKey: String = "",
    val customProxyBaseUrl: String = "",
    val customModelName: String = "gemini-2.5-flash",
    val selfPacedMode: Boolean = false
) {
    val isCustomRouteActive: Boolean
        get() = customApiKey.isNotBlank() || customProxyBaseUrl.isNotBlank()
}
