package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class LearningRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProgress(userId: String): Flow<UserProgress?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch." }
        val docRef = db.collection("users").document(userId)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    snapshot.toObject(
                        UserProgress::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    fun observeLessons(userId: String): Flow<List<LessonRecord>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch." }
        val colRef = db.collection("users").document(userId).collection("lessons")
        emitAll(
            colRef.orderBy("dayNumber", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        LessonRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureInitialized(userId: String, displayName: String): Result<Unit> {
        val userDocPath = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch." }
            val userRef = db.collection("users").document(userId)
            val userSnap = userRef.get().await()
            if (!userSnap.exists()) {
                val safeName = displayName.trim().take(100).ifBlank { "AI Engineer" }
                val progressPayload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "currentDay" to 1L,
                    "highestCompletedDay" to 0L,
                    "streakDays" to 0L,
                    "totalXp" to 0L,
                    "correctAnswers" to 0L,
                    "totalAnswers" to 0L,
                    "nextUnlockEpochMs" to 0L,
                    "yesterdayQuizPassedForDay" to 1L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userRef.set(progressPayload).await()
            }

            val day1Ref = userRef.collection("lessons").document("day_1")
            val day1Snap = day1Ref.get().await()
            if (!day1Snap.exists()) {
                val day1 = PrebuiltCurriculum.getPrebuiltLesson(1L)!!
                day1Ref.set(lessonToCreateMap(day1)).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userDocPath)
            Result.failure(e)
        }
    }

    suspend fun saveLesson(userId: String, lesson: LessonRecord): Result<Unit> {
        val docId = "day_${lesson.dayNumber}"
        val path = "users/$userId/lessons/$docId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch." }
            val docRef = db.collection("users").document(userId)
                .collection("lessons").document(docId)
            val existing = docRef.get().await()
            if (!existing.exists()) {
                docRef.set(lessonToCreateMap(lesson)).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun saveDeepDive(
        userId: String,
        dayNumber: Long,
        deepDiveContent: String
    ): Result<Unit> {
        val docId = "day_$dayNumber"
        val path = "users/$userId/lessons/$docId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch." }
            val trimmed = deepDiveContent.take(45000)
            db.collection("users").document(userId)
                .collection("lessons").document(docId)
                .update(
                    mapOf(
                        "deepDiveContent" to trimmed,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun recordYesterdayQuizAnswer(
        userId: String,
        targetDay: Long,
        isCorrect: Boolean,
        currentProgress: UserProgress
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch." }
            val newCorrect = currentProgress.correctAnswers + if (isCorrect) 1L else 0L
            val newTotal = currentProgress.totalAnswers + 1L
            val bonusXp = if (isCorrect) 25L else 10L
            db.collection("users").document(userId)
                .update(
                    mapOf(
                        "correctAnswers" to newCorrect,
                        "totalAnswers" to newTotal,
                        "totalXp" to (currentProgress.totalXp + bonusXp),
                        "yesterdayQuizPassedForDay" to maxOf(
                            currentProgress.yesterdayQuizPassedForDay,
                            targetDay
                        ),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun completeDay(
        userId: String,
        lesson: LessonRecord,
        currentProgress: UserProgress,
        quizScore: Long,
        quizCorrectCount: Long,
        quizTotalCount: Long
    ): Result<Unit> {
        val docId = "day_${lesson.dayNumber}"
        val path = "users/$userId/lessons/$docId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch." }
            val userRef = db.collection("users").document(userId)
            val lessonRef = userRef.collection("lessons").document(docId)

            val batch = db.batch()
            batch.update(
                lessonRef,
                mapOf(
                    "completed" to true,
                    "quizScore" to quizScore.coerceIn(0L, 10L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            val nextDay = (lesson.dayNumber + 1L).coerceAtMost(3650L)
            val unlockAt = System.currentTimeMillis() + TWENTY_FOUR_HOURS_MS
            val earnedXp = if (lesson.completed) 0L else lesson.xpReward
            val newHighest = maxOf(currentProgress.highestCompletedDay, lesson.dayNumber)
            val newStreak = if (lesson.completed) currentProgress.streakDays else currentProgress.streakDays + 1L
            val newCorrect = currentProgress.correctAnswers + quizCorrectCount
            val newTotal = currentProgress.totalAnswers + quizTotalCount
            val passedForDay = if (nextDay % 7L == 0L) {
                // Day 7 is itself a consolidated weekly assessment; no separate 1-question gate needed
                maxOf(currentProgress.yesterdayQuizPassedForDay, nextDay)
            } else {
                currentProgress.yesterdayQuizPassedForDay
            }

            batch.update(
                userRef,
                mapOf(
                    "currentDay" to maxOf(currentProgress.currentDay, nextDay),
                    "highestCompletedDay" to newHighest,
                    "streakDays" to newStreak,
                    "totalXp" to (currentProgress.totalXp + earnedXp),
                    "correctAnswers" to newCorrect,
                    "totalAnswers" to newTotal,
                    "nextUnlockEpochMs" to unlockAt,
                    "yesterdayQuizPassedForDay" to passedForDay,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    private fun lessonToCreateMap(lesson: LessonRecord): Map<String, Any> {
        val weeklyMaps = lesson.weeklyQuestions.take(10).map { q ->
            mapOf(
                "question" to q.question.take(1000),
                "options" to q.options.take(6),
                "correctAnswerIndex" to q.correctAnswerIndex.coerceIn(0L, 5L),
                "topicTag" to q.topicTag.take(80),
                "explanation" to q.explanation.take(1000)
            )
        }
        return mapOf(
            "dayNumber" to lesson.dayNumber.coerceIn(1L, 3650L),
            "topic" to lesson.topic.take(240).ifBlank { "Урок дня ${lesson.dayNumber}" },
            "category" to lesson.category.take(80).ifBlank { "AI ENGINEERING" },
            "summary" to lesson.summary.take(1000),
            "content" to lesson.content.take(35000).ifBlank { "Контент урока." },
            "question" to lesson.question.take(1500),
            "options" to lesson.options.take(6),
            "correctAnswerIndex" to lesson.correctAnswerIndex.coerceIn(0L, 5L),
            "explanation" to lesson.explanation.take(3000),
            "deepDiveContent" to lesson.deepDiveContent.take(45000),
            "weeklyAssessment" to lesson.weeklyAssessment,
            "weeklyQuestions" to weeklyMaps,
            "xpReward" to lesson.xpReward.coerceIn(0L, 5000L),
            "estimatedMinutes" to lesson.estimatedMinutes.coerceIn(1L, 120L),
            "completed" to lesson.completed,
            "quizScore" to lesson.quizScore.coerceIn(0L, 10L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    companion object {
        const val TWENTY_FOUR_HOURS_MS = 24L * 60L * 60L * 1000L
    }
}
