package com.example.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null,
    val maxOutputTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

// --- Task Parser Structure (Extracted from prompt) ---
@JsonClass(generateAdapter = true)
data class ExtractedTask(
    val title: String,
    val category: String, // Work, Study, Personal, Health, Other
    val priority: String,  // Low, Medium, High, Urgent
    val estimatedMinutes: Int,
    val date: String,      // YYYY-MM-DD
    val timeSlot: String?,  // e.g. "18:00"
    val emoji: String
)
