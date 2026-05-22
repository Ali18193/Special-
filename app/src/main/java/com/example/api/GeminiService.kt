package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    val moshiInstance: Moshi get() = moshi
}

class GeminiService(private val customApiKeyProvider: () -> String?) {

    private val tag = "GeminiService"

    private fun getApiKey(): String {
        val customKey = customApiKeyProvider()
        if (!customKey.isNullOrBlank()) return customKey
        return BuildConfig.GEMINI_API_KEY
    }

    /**
     * Splits and parses natural language task requests into structured ExtractedTask configurations.
     */
    suspend fun parseTask(input: String, localDateToday: String): List<ExtractedTask> {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(tag, "API Key is empty. Falling back to simple offline regex parser.")
            return parseTaskOffline(input, localDateToday)
        }

        val prompt = """
            Today's date is $localDateToday.
            Parse the following user request and extract one or more planning tasks:
            "$input"
            
            Respond only with a JSON array where each element matches this exact JSON structure:
            {
              "title": "Short title text (max 6 words)",
              "category": "Work" OR "Study" OR "Personal" OR "Health" OR "Other",
              "priority": "Low" OR "Medium" OR "High" OR "Urgent",
              "estimatedMinutes": integer_duration,
              "date": "YYYY-MM-DD",
              "timeSlot": "HH:MM" or null,
              "emoji": "appropriate single emoji"
            }
            
            Do not include any greeting or conversational filler. Return raw JSON text only.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            )
        )

        return try {
            val response = GeminiClient.api.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = request
            )
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!jsonText.isNullOrEmpty()) {
                val cleanJson = cleanJsonOutput(jsonText)
                val type = Types.newParameterizedType(List::class.java, ExtractedTask::class.java)
                val adapter = GeminiClient.moshiInstance.adapter<List<ExtractedTask>>(type)
                adapter.fromJson(cleanJson) ?: parseTaskOffline(input, localDateToday)
            } else {
                parseTaskOffline(input, localDateToday)
            }
        } catch (e: Exception) {
            Log.e(tag, "Gemini parsing failed, falling back downstream: ${e.message}")
            parseTaskOffline(input, localDateToday)
        }
    }

    /**
     * Generates a daily motivational summary based on completed vs missed tasks.
     */
    suspend fun generateDailySummary(
        completedCount: Int,
        missedCount: Int,
        completedTasksStr: String,
        missedTasksStr: String
    ): String {
        val apiKey = getApiKey()
        val defaultText = "Completed $completedCount tasks today ($completedTasksStr). Missed $missedCount tasks ($missedTasksStr). Great job staying on track, review tomorrow's plan!"
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return defaultText
        }

        val prompt = """
            Write a short, professional, motivating 3-sentence daily review.
            Metrics:
            - Completed: $completedCount ($completedTasksStr)
            - Missed/Postponed: $missedCount ($missedTasksStr)
            
            Identify achievements and offer a gentle workspace/reschedule suggestion for tomorrow. Keep it under 100 words. No markdowns or hashtags.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        return try {
            val response = GeminiClient.api.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = request
            )
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: defaultText
        } catch (e: Exception) {
            defaultText
        }
    }

    /**
     * Recommends optimal focus hours and schedule suggestions based on historical performance.
     */
    suspend fun generateSuggestions(historyDataSummary: String): String {
        val apiKey = getApiKey()
        val defaultMessage = "Schedule highly demanding projects (Work/Study) in quiet mornings. Plan short Personal review slots after lunch."
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return defaultMessage
        }

        val prompt = """
            Analyze this user's productivity history summaries and provide a 2-sentence actionable advice:
            "$historyDataSummary"
            
            Specifically suggest optimal time slot allocations or categories to focus on next.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.5f)
        )

        return try {
            val response = GeminiClient.api.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = request
            )
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: defaultMessage
        } catch (e: Exception) {
            defaultMessage
        }
    }

    /**
     * Robust offline fallback parsing mechanism utilizing Regex and basic string rules.
     */
    fun parseTaskOffline(input: String, localDateToday: String): List<ExtractedTask> {
        val parts = if (input.contains(" and also ") || input.contains(", then ") || input.contains("; ")) {
            input.split(Regex(" (and also|, then|;) ")).map { it.trim() }
        } else if (input.lines().size > 1) {
            input.lines().map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            listOf(input)
        }

        val parsedList = mutableListOf<ExtractedTask>()
        val sdf = SimpleDateFormat("yyyy-MM-DD", Locale.getDefault())
        val defaultDateStr = localDateToday

        for (item in parts) {
            // Priority detection
            var priority = "Medium"
            if (item.contains("urgent", ignoreCase = true) || item.contains("asap", ignoreCase = true)) {
                priority = "Urgent"
            } else if (item.contains("important", ignoreCase = true) || item.contains("high", ignoreCase = true)) {
                priority = "High"
            } else if (item.contains("low", ignoreCase = true) || item.contains("whenever", ignoreCase = true)) {
                priority = "Low"
            }

            // Category detection
            var category = "Personal"
            var emoji = "📝"
            if (item.contains("work", ignoreCase = true) || item.contains("report", ignoreCase = true) || item.contains("slide", ignoreCase = true) || item.contains("meeting", ignoreCase = true)) {
                category = "Work"
                emoji = "💻"
            } else if (item.contains("study", ignoreCase = true) || item.contains("homework", ignoreCase = true) || item.contains("math", ignoreCase = true) || item.contains("exam", ignoreCase = true) || item.contains("read", ignoreCase = true)) {
                category = "Study"
                emoji = "📚"
            } else if (item.contains("call", ignoreCase = true) || item.contains("mom", ignoreCase = true) || item.contains("friend", ignoreCase = true)) {
                category = "Personal"
                emoji = "📞"
            } else if (item.contains("health", ignoreCase = true) || item.contains("gym", ignoreCase = true) || item.contains("run", ignoreCase = true) || item.contains("doctor", ignoreCase = true) || item.contains("sport", ignoreCase = true)) {
                category = "Health"
                emoji = "🏃‍♂️"
            }

            // Time Slot detection
            var timeSlot: String? = null
            val timeRegex = Regex("(?i)\\b(\\d{1,2})\\s*(?:am|pm|:(\\d{2}))")
            val rawTimeMatch = timeRegex.find(item)
            if (rawTimeMatch != null) {
                val hourStr = rawTimeMatch.groupValues[1]
                var hour = hourStr.toInt()
                val isPm = item.contains("pm", ignoreCase = true)
                if (isPm && hour < 12) hour += 12
                if (!isPm && hour == 12) hour = 0
                val minStr = if (rawTimeMatch.groupValues[2].isNotEmpty()) rawTimeMatch.groupValues[2] else "00"
                timeSlot = String.format(Locale.getDefault(), "%02d:%s", hour, minStr)
            }

            // Date relative parsing simple checks
            var dateStr = defaultDateStr
            if (item.contains("tomorrow", ignoreCase = true)) {
                try {
                    val cal = Calendar.getInstance()
                    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val current = sdfDate.parse(localDateToday)
                    if (current != null) {
                        cal.time = current
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        dateStr = sdfDate.format(cal.time)
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            } else if (item.contains("yesterday", ignoreCase = true)) {
                try {
                    val cal = Calendar.getInstance()
                    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val current = sdfDate.parse(localDateToday)
                    if (current != null) {
                        cal.time = current
                        cal.add(Calendar.DAY_OF_YEAR, -1)
                        dateStr = sdfDate.format(cal.time)
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }

            // Duration estimation
            var estMin = 25
            if (item.contains("mins", ignoreCase = true) || item.contains("minutes", ignoreCase = true)) {
                val durationRegex = Regex("(\\d+)\\s*(?:min|minute)")
                val match = durationRegex.find(item)
                if (match != null) {
                    estMin = match.groupValues[1].toInt()
                }
            } else if (item.contains("hour", ignoreCase = true)) {
                estMin = 60
            }

            // Clean title words limit
            val words = item.split(" ")
            val cleanTitle = words.take(5).joinToString(" ").replace(Regex("[.,;:!?]"), "")

            parsedList.add(
                ExtractedTask(
                    title = if (cleanTitle.length > 3) cleanTitle else "New Task",
                    category = category,
                    priority = priority,
                    estimatedMinutes = estMin,
                    date = dateStr,
                    timeSlot = timeSlot,
                    emoji = emoji
                )
            )
        }
        return parsedList
    }

    private fun cleanJsonOutput(raw: String): String {
        // Strip markdown backticks if returned (e.g. ```json ... ```)
        var cleaned = raw.trim()
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substringAfter("\n").substringBeforeLast("```").trim()
            if (cleaned.startsWith("json")) {
                cleaned = cleaned.removePrefix("json").trim()
            }
        }
        return cleaned
    }
}
