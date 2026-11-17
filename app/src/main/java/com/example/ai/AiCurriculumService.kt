package com.example.ai

import android.util.Log
import com.example.data.LessonRecord
import com.example.data.ProxySettings
import com.example.data.WeeklyQuestion
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

data class SandboxResult(
    val text: String,
    val latencyMs: Long,
    val routeLabel: String
)

data class ProxyProbeResult(
    val success: Boolean,
    val statusCode: Int?,
    val latencyMs: Long,
    val message: String
)

class AiCurriculumService {

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val generalModel by lazy {
        Firebase.ai.generativeModel(modelName = DEFAULT_FIREBASE_MODEL)
    }

    private val lessonSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "topic" to Schema.string(description = "Concise Russian title of the lesson"),
                "category" to Schema.string(description = "Short uppercase category tag e.g. AGENTS, RAG, MCP, SECURITY, INFRA"),
                "summary" to Schema.string(description = "1-2 sentence summary in Russian"),
                "content" to Schema.string(description = "Rich structured Markdown lesson in Russian with headings, bullet points, and code/architecture examples"),
                "question" to Schema.string(description = "1 review quiz question in Russian testing understanding of this lesson"),
                "options" to Schema.array(
                    items = Schema.string(),
                    description = "Exactly 4 answer choices in Russian"
                ),
                "correct_answer" to Schema.integer(description = "Zero-based index (0 to 3) of the correct option"),
                "explanation" to Schema.string(description = "Brief explanation in Russian why the correct answer is right"),
                "estimated_minutes" to Schema.integer(description = "Estimated reading time in minutes (4 to 8)")
            )
        )
    }

    private val lessonJsonModel by lazy {
        Firebase.ai.generativeModel(
            modelName = DEFAULT_FIREBASE_MODEL,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = lessonSchema
            }
        )
    }

    private val weeklySchema by lazy {
        Schema.obj(
            properties = mapOf(
                "topic" to Schema.string(description = "Russian title of the weekly knowledge check"),
                "summary" to Schema.string(description = "Short summary of the weekly assessment in Russian"),
                "questions" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "topic_tag" to Schema.string(description = "Short topic badge"),
                            "question" to Schema.string(description = "Question text in Russian"),
                            "options" to Schema.array(
                                items = Schema.string(),
                                description = "4 answer choices in Russian"
                            ),
                            "correct_answer" to Schema.integer(description = "Zero-based index 0..3"),
                            "explanation" to Schema.string(description = "Short explanation in Russian")
                        )
                    ),
                    description = "Exactly 6 consolidated quiz questions covering the past 6 days"
                )
            )
        )
    }

    private val weeklyJsonModel by lazy {
        Firebase.ai.generativeModel(
            modelName = DEFAULT_FIREBASE_MODEL,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = weeklySchema
            }
        )
    }

    fun generateDeepDiveStream(
        lesson: LessonRecord,
        settings: ProxySettings
    ): Flow<String> = flow {
        val prompt = buildDeepDivePrompt(lesson)
        if (settings.isCustomRouteActive) {
            val responseText = withTimeout(60_000L) {
                executeCustomGeminiRest(
                    prompt = prompt,
                    settings = settings,
                    responseSchemaJson = null
                )
            }
            emit(responseText)
        } else {
            val sb = StringBuilder()
            withTimeout(60_000L) {
                generalModel.generateContentStream(prompt).collect { chunk ->
                    val chunkText = chunk.text.orEmpty()
                    if (chunkText.isNotEmpty()) {
                        sb.append(chunkText)
                        emit(sb.toString())
                    }
                }
            }
            if (sb.isBlank()) {
                throw IllegalStateException("Модель вернула пустой ответ для Deep Dive.")
            }
        }
    }

    suspend fun generateDynamicLesson(
        dayNumber: Long,
        previousTopics: List<String>,
        settings: ProxySettings
    ): LessonRecord {
        val prompt = buildDynamicLessonPrompt(dayNumber, previousTopics)
        val jsonText = withTimeout(60_000L) {
            if (settings.isCustomRouteActive) {
                executeCustomGeminiRest(
                    prompt = prompt,
                    settings = settings,
                    responseSchemaJson = buildDynamicLessonRestSchema()
                )
            } else {
                val resp = lessonJsonModel.generateContent(prompt)
                resp.text ?: throw IllegalStateException("Модель вернула пустой JSON урока.")
            }
        }
        return parseDynamicLessonJson(dayNumber, jsonText)
    }

    suspend fun generateWeeklyAssessment(
        dayNumber: Long,
        pastLessons: List<LessonRecord>,
        settings: ProxySettings
    ): LessonRecord {
        val prompt = buildWeeklyAssessmentPrompt(dayNumber, pastLessons)
        val jsonText = withTimeout(60_000L) {
            if (settings.isCustomRouteActive) {
                executeCustomGeminiRest(
                    prompt = prompt,
                    settings = settings,
                    responseSchemaJson = buildWeeklyRestSchema()
                )
            } else {
                val resp = weeklyJsonModel.generateContent(prompt)
                resp.text ?: throw IllegalStateException("Модель вернула пустой JSON среза знаний.")
            }
        }
        return parseWeeklyAssessmentJson(dayNumber, jsonText)
    }

    suspend fun runSandboxPrompt(
        prompt: String,
        settings: ProxySettings
    ): SandboxResult {
        val start = System.currentTimeMillis()
        val routeLabel = if (settings.isCustomRouteActive) {
            val host = settings.customProxyBaseUrl.ifBlank { DEFAULT_PUBLIC_BASE_URL }
                .removePrefix("https://").removePrefix("http://").substringBefore("/")
            "Custom Proxy • $host"
        } else {
            "Firebase AI Logic • $DEFAULT_FIREBASE_MODEL"
        }

        val text = withTimeout(60_000L) {
            if (settings.isCustomRouteActive) {
                executeCustomGeminiRest(
                    prompt = prompt,
                    settings = settings,
                    responseSchemaJson = null
                )
            } else {
                val resp = generalModel.generateContent(prompt)
                resp.text ?: throw IllegalStateException("Пустой ответ от модели.")
            }
        }
        val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1L)
        return SandboxResult(text = text, latencyMs = elapsed, routeLabel = routeLabel)
    }

    suspend fun testConnection(settings: ProxySettings): ProxyProbeResult {
        val start = System.currentTimeMillis()
        return try {
            withTimeout(25_000L) {
                if (settings.isCustomRouteActive) {
                    val text = executeCustomGeminiRest(
                        prompt = "Ответь одним словом: OK",
                        settings = settings,
                        responseSchemaJson = null
                    )
                    val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1L)
                    ProxyProbeResult(
                        success = text.isNotBlank(),
                        statusCode = 200,
                        latencyMs = elapsed,
                        message = "Прокси/REST канал активен (${elapsed} мс): ${text.trim().take(40)}"
                    )
                } else {
                    val resp = generalModel.generateContent("Ответь одним словом: OK")
                    val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1L)
                    ProxyProbeResult(
                        success = !resp.text.isNullOrBlank(),
                        statusCode = 200,
                        latencyMs = elapsed,
                        message = "Firebase AI Logic активен (${elapsed} мс): ${resp.text?.trim()?.take(40) ?: "OK"}"
                    )
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1L)
            Log.e(TAG, "Connection test failed", e)
            ProxyProbeResult(
                success = false,
                statusCode = null,
                latencyMs = elapsed,
                message = formatRegionalError(e, settings.isCustomRouteActive)
            )
        }
    }

    private suspend fun executeCustomGeminiRest(
        prompt: String,
        settings: ProxySettings,
        responseSchemaJson: JSONObject?
    ): String = withContext(Dispatchers.IO) {
        val baseUrl = com.example.data.LocalSettingsRepository
            .normalizeProxyBaseUrl(settings.customProxyBaseUrl)
            .ifBlank { DEFAULT_PUBLIC_BASE_URL }
        val model = settings.customModelName.trim().ifBlank { DEFAULT_CUSTOM_MODEL }
        val endpoint = "$baseUrl/v1beta/models/$model:generateContent"

        val bodyJson = JSONObject().apply {
            put(
                "contents",
                JSONArray().put(
                    JSONObject().put(
                        "parts",
                        JSONArray().put(JSONObject().put("text", prompt))
                    )
                )
            )
            if (responseSchemaJson != null) {
                put(
                    "generationConfig",
                    JSONObject().apply {
                        put("responseMimeType", "application/json")
                        put("responseSchema", responseSchemaJson)
                    }
                )
            }
        }

        val reqBuilder = Request.Builder()
            .url(endpoint)
            .post(bodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .header("Content-Type", "application/json")

        if (settings.customApiKey.isNotBlank()) {
            reqBuilder.header("x-goog-api-key", settings.customApiKey.trim())
        }

        httpClient.newCall(reqBuilder.build()).execute().use { response ->
            val rawBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val apiError = runCatching {
                    JSONObject(rawBody).optJSONObject("error")?.optString("message")
                }.getOrNull()
                throw IllegalStateException(
                    apiError?.takeIf { it.isNotBlank() }
                        ?: "HTTP ${response.code}: проверьте Proxy Base URL или API-ключ."
                )
            }
            val parsed = JSONObject(rawBody)
            val candidates = parsed.optJSONArray("candidates")
                ?: throw IllegalStateException("Ответ Gemini не содержит candidates.")
            val first = candidates.optJSONObject(0)
                ?: throw IllegalStateException("Пустой список candidates.")
            val parts = first.optJSONObject("content")?.optJSONArray("parts")
                ?: throw IllegalStateException("Ответ не содержит content.parts.")
            val text = (0 until parts.length())
                .mapNotNull { parts.optJSONObject(it)?.optString("text") }
                .joinToString("")
            if (text.isBlank()) {
                throw IllegalStateException("Модель вернула пустой текст.")
            }
            text
        }
    }

    private fun parseDynamicLessonJson(dayNumber: Long, jsonText: String): LessonRecord {
        val json = JSONObject(jsonText)
        val topic = json.getString("topic").take(220)
        val category = json.optString("category", "AI AGENTS").uppercase().take(40)
        val summary = json.optString("summary", "").take(900)
        val content = json.getString("content").take(32000)
        val question = json.getString("question").take(1400)
        val optionsArr = json.getJSONArray("options")
        val options = (0 until optionsArr.length()).map { optionsArr.getString(it) }.take(6)
        val correctIdx = json.optLong("correct_answer", 0L).coerceIn(0L, (options.size - 1).coerceAtLeast(0).toLong())
        val explanation = json.optString("explanation", "").take(2500)
        val minutes = json.optLong("estimated_minutes", 6L).coerceIn(3L, 20L)

        return LessonRecord(
            dayNumber = dayNumber,
            topic = topic,
            category = category,
            summary = summary,
            content = content,
            question = question,
            options = options,
            correctAnswerIndex = correctIdx,
            explanation = explanation,
            deepDiveContent = "",
            weeklyAssessment = false,
            weeklyQuestions = emptyList(),
            xpReward = 140L,
            estimatedMinutes = minutes,
            completed = false,
            quizScore = 0L
        )
    }

    private fun parseWeeklyAssessmentJson(dayNumber: Long, jsonText: String): LessonRecord {
        val json = JSONObject(jsonText)
        val topic = json.optString("topic", "Еженедельный срез знаний • День $dayNumber").take(220)
        val summary = json.optString("summary", "Сводная проверка ключевых концепций за 6 дней.").take(900)
        val qArr = json.getJSONArray("questions")
        val questions = (0 until qArr.length()).map { idx ->
            val item = qArr.getJSONObject(idx)
            val optsArr = item.getJSONArray("options")
            val opts = (0 until optsArr.length()).map { optsArr.getString(it) }.take(6)
            val correct = item.optLong("correct_answer", 0L).coerceIn(0L, (opts.size - 1).coerceAtLeast(0).toLong())
            WeeklyQuestion(
                question = item.getString("question").take(1000),
                options = opts,
                correctAnswerIndex = correct,
                topicTag = item.optString("topic_tag", "REVIEW").uppercase().take(50),
                explanation = item.optString("explanation", "").take(1000)
            )
        }.take(10)

        return LessonRecord(
            dayNumber = dayNumber,
            topic = topic,
            category = "WEEKLY SPRINT",
            summary = summary,
            content = "Сводное тестирование по материалам последних 6 дней. Ответьте на вопросы, чтобы закрепить знания и заработать бонусный XP.",
            question = "",
            options = emptyList(),
            correctAnswerIndex = 0L,
            explanation = "",
            deepDiveContent = "",
            weeklyAssessment = true,
            weeklyQuestions = questions,
            xpReward = 250L,
            estimatedMinutes = 8L,
            completed = false,
            quizScore = 0L
        )
    }

    private fun buildDeepDivePrompt(lesson: LessonRecord): String = """
Ты — ведущий архитектор ИИ-инфраструктуры и автор курса Daily AI Mastery.
Пользователь изучает урок Дня ${lesson.dayNumber}: «${lesson.topic}».
Краткое описание урока: ${lesson.summary}

Сгенерируй углублённый технический разбор (Deep Dive) на русском языке в формате Markdown:
1. Подробная архитектурная схема или механизм под капотом (без воды).
2. Практический пример кода или конфигурации (Kotlin / Python / TypeScript / HTTP / YAML) в блоке ```...```.
3. Типичные ошибки в продакшене и чек-лист отладки.

Ограничься ~450-650 словами, пиши чётко и прикладным языком.
    """.trimIndent()

    private fun buildDynamicLessonPrompt(dayNumber: Long, previousTopics: List<String>): String {
        val history = previousTopics.takeLast(8).joinToString("; ")
        return """
Ты — методист инженерного микро-курса «Daily AI Mastery».
Пользователь уже прошёл темы: $history.
Сгенерируй новый практический урок для Дня $dayNumber на русском языке.
Подходящие темы для продолжения после базы (сети, прокси, VPN, безопасность, MCP):
- Построение мультиагентных систем (Orchestrator / Worker, Tool Calling, Structured Outputs)
- RAG-архитектура: embeddings, chunking, hybrid search и reranking
- Наблюдаемость и оценка LLM (Evals, OpenTelemetry, Latency/Token budgeting)
- Локальные модели, квантизация GGUF и гибридный роутинг (Edge + Cloud)
- Продвинутый MCP: OAuth, stateful sessions и безопасность песочниц для кода

Выбери тему, которая логично развивает программу и ещё не повторялась.
В поле `content` используй Markdown с заголовками `##`, `###`, списками и минимум одним практическим блоком кода ```...```.
В `options` верни ровно 4 варианта ответа, а в `correct_answer` — индекс (0..3) правильного ответа.
        """.trimIndent()
    }

    private fun buildWeeklyAssessmentPrompt(dayNumber: Long, pastLessons: List<LessonRecord>): String {
        val topicsSummary = pastLessons.takeLast(6).joinToString("\n") {
            "- День ${it.dayNumber} (${it.category}): ${it.topic} — ${it.summary}"
        }
        return """
Ты — экзаменатор курса «Daily AI Mastery».
Сегодня День $dayNumber — еженедельный срез знаний по темам последних 6 дней:
$topicsSummary

Сгенерируй ровно 6 разнообразных, нетривиальных тестовых вопросов на русском языке (по одному на ключевую тему из списка), проверяющих реальное инженерное понимание.
Для каждого вопроса укажи `topic_tag`, `question`, ровно 4 варианта в `options`, `correct_answer` (0..3) и краткое `explanation`.
        """.trimIndent()
    }

    private fun buildDynamicLessonRestSchema(): JSONObject = JSONObject().apply {
        put("type", "OBJECT")
        put(
            "properties",
            JSONObject().apply {
                put("topic", JSONObject().put("type", "STRING"))
                put("category", JSONObject().put("type", "STRING"))
                put("summary", JSONObject().put("type", "STRING"))
                put("content", JSONObject().put("type", "STRING"))
                put("question", JSONObject().put("type", "STRING"))
                put(
                    "options",
                    JSONObject().apply {
                        put("type", "ARRAY")
                        put("items", JSONObject().put("type", "STRING"))
                    }
                )
                put("correct_answer", JSONObject().put("type", "INTEGER"))
                put("explanation", JSONObject().put("type", "STRING"))
                put("estimated_minutes", JSONObject().put("type", "INTEGER"))
            }
        )
        put(
            "required",
            JSONArray(
                listOf(
                    "topic",
                    "category",
                    "summary",
                    "content",
                    "question",
                    "options",
                    "correct_answer",
                    "explanation",
                    "estimated_minutes"
                )
            )
        )
    }

    private fun buildWeeklyRestSchema(): JSONObject = JSONObject().apply {
        put("type", "OBJECT")
        put(
            "properties",
            JSONObject().apply {
                put("topic", JSONObject().put("type", "STRING"))
                put("summary", JSONObject().put("type", "STRING"))
                put(
                    "questions",
                    JSONObject().apply {
                        put("type", "ARRAY")
                        put(
                            "items",
                            JSONObject().apply {
                                put("type", "OBJECT")
                                put(
                                    "properties",
                                    JSONObject().apply {
                                        put("topic_tag", JSONObject().put("type", "STRING"))
                                        put("question", JSONObject().put("type", "STRING"))
                                        put(
                                            "options",
                                            JSONObject().apply {
                                                put("type", "ARRAY")
                                                put("items", JSONObject().put("type", "STRING"))
                                            }
                                        )
                                        put("correct_answer", JSONObject().put("type", "INTEGER"))
                                        put("explanation", JSONObject().put("type", "STRING"))
                                    }
                                )
                                put(
                                    "required",
                                    JSONArray(listOf("topic_tag", "question", "options", "correct_answer", "explanation"))
                                )
                            }
                        )
                    }
                )
            }
        )
        put("required", JSONArray(listOf("topic", "summary", "questions")))
    }

    companion object {
        private const val TAG = "AiCurriculumService"
        private const val DEFAULT_FIREBASE_MODEL = "gemini-3.8-flash"
        private const val DEFAULT_CUSTOM_MODEL = "gemini-2.5-flash"
        private const val DEFAULT_PUBLIC_BASE_URL = "https://generativelanguage.googleapis.com"

        fun formatRegionalError(error: Throwable, isCustomRouteActive: Boolean): String {
            val raw = error.message.orEmpty()
            val lower = raw.lowercase()
            val isGeoBlocked = lower.contains("location is not supported") ||
                lower.contains("failed_precondition") ||
                lower.contains("country") ||
                lower.contains("region")
            val isNetworkBlocked = error is java.net.UnknownHostException ||
                error is java.net.SocketTimeoutException ||
                error is java.net.ConnectException ||
                error is javax.net.ssl.SSLException

            return when {
                isGeoBlocked && !isCustomRouteActive ->
                    "В текущем регионе прямой доступ к Gemini ограничен ($raw). Укажите свой Proxy Base URL (например, Cloudflare Workers) в разделе «Профиль»."
                isGeoBlocked && isCustomRouteActive ->
                    "Сервер прокси или выбранный регион отклонён API Gemini: $raw. Проверьте, что ваш Worker развёрнут в поддерживаемом регионе (США/ЕС)."
                isNetworkBlocked && !isCustomRouteActive ->
                    "Сетевой сбой или блокировка провайдером ($raw). Попробуйте настроить кастомный Proxy Base URL в разделе «Профиль»."
                isNetworkBlocked && isCustomRouteActive ->
                    "Не удалось связаться с Proxy URL ($raw). Проверьте адрес прокси и подключение к сети."
                else -> raw.ifBlank { "Ошибка обращения к ИИ-модели." }
            }
        }
    }
}
