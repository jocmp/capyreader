package com.jocmp.capy.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

@Serializable
data class AiMessage(
    val role: String,
    val content: String
)

@Serializable
data class AiChatRequest(
    val model: String,
    val messages: List<AiMessage>,
)

@Serializable
data class AiChoice(
    val message: AiMessage
)

@Serializable
data class AiChatResponse(
    val choices: List<AiChoice>
)

@Serializable
data class AiModel(
    val id: String
)

@Serializable
data class AiModelsResponse(
    val data: List<AiModel>
)

class AiSummaryClient(
    private val apiKey: String,
    private val baseUrl: String,
    private val httpClient: OkHttpClient = OkHttpClient.Builder().build()
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateSummary(
        articleHtml: String,
        model: String,
        systemPrompt: String,
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("API Key is missing"))
        }
        if (baseUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Base URL is missing"))
        }

        val requestBody = AiChatRequest(
            model = model,
            messages = listOf(
                AiMessage(role = "system", content = systemPrompt),
                AiMessage(role = "user", content = articleHtml)
            )
        )

        val jsonBody = try {
            json.encodeToString(requestBody)
        } catch (e: Exception) {
            return@withContext Result.failure(Exception("Failed to encode request: ${e.message}"))
        }
        
        val url = buildUrl("chat/completions")

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toRequestBody(mediaType))
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()

        return@withContext try {
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (response.isSuccessful) {
                if (responseBody != null) {
                    val chatResponse = json.decodeFromString<AiChatResponse>(responseBody)
                    val summary = chatResponse.choices.firstOrNull()?.message?.content
                    if (summary != null) {
                        Result.success(markdownToHtml(summary))
                    } else {
                        Result.failure(Exception("No summary found in response. Status: ${response.code}"))
                    }
                } else {
                    Result.failure(Exception("Empty response body from API"))
                }
            } else {
                val errorMessage = responseBody ?: response.message
                Result.failure(IOException("API Error ${response.code}: $errorMessage"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchModels(): Result<List<String>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || baseUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("API Key or Base URL is missing"))
        }

        val url = buildUrl("models")

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        return@withContext try {
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                if (responseBody != null) {
                    val modelsResponse = json.decodeFromString<AiModelsResponse>(responseBody)
                    Result.success(modelsResponse.data.map { it.id }.sorted())
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = responseBody ?: response.message
                Result.failure(IOException("API Error ${response.code}: $errorMessage"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildUrl(path: String): String {
        return baseUrl.trimEnd('/') + "/" + path
    }

    companion object {
        fun markdownToHtml(markdown: String): String {
            val lines = markdown.lines()
            val sb = StringBuilder()
            var inUl = false
            var inOl = false

            fun closeList() {
                if (inUl) { sb.append("</ul>\n"); inUl = false }
                if (inOl) { sb.append("</ol>\n"); inOl = false }
            }

            fun inlineFormat(text: String): String {
                return text
                    .replace(Regex("\\*\\*\\*(.+?)\\*\\*\\*")) { "<strong><em>${it.groupValues[1]}</em></strong>" }
                    .replace(Regex("\\*\\*(.+?)\\*\\*")) { "<strong>${it.groupValues[1]}</strong>" }
                    .replace(Regex("\\*(.+?)\\*")) { "<em>${it.groupValues[1]}</em>" }
                    .replace(Regex("`(.+?)`")) { "<code>${it.groupValues[1]}</code>" }
            }

            for (line in lines) {
                val trimmed = line.trim()
                when {
                    trimmed.matches(Regex("#{1,6}\\s+.*")) -> {
                        closeList()
                        val level = trimmed.takeWhile { it == '#' }.length
                        val text = trimmed.dropWhile { it == '#' }.trim()
                        sb.append("<h$level>${inlineFormat(text)}</h$level>\n")
                    }
                    trimmed.matches(Regex("[\\-\\*•]\\s+.*")) -> {
                        if (inOl) closeList()
                        if (!inUl) { sb.append("<ul>\n"); inUl = true }
                        val text = trimmed.dropWhile { it == '-' || it == '*' || it == '•' }.trim()
                        sb.append("<li>${inlineFormat(text)}</li>\n")
                    }
                    trimmed.matches(Regex("\\d+\\.\\s+.*")) -> {
                        if (inUl) closeList()
                        if (!inOl) { sb.append("<ol>\n"); inOl = true }
                        val text = trimmed.dropWhile { it.isDigit() || it == '.' }.trim()
                        sb.append("<li>${inlineFormat(text)}</li>\n")
                    }
                    trimmed.isEmpty() -> {
                        closeList()
                        sb.append("<br>\n")
                    }
                    else -> {
                        closeList()
                        sb.append("<p>${inlineFormat(trimmed)}</p>\n")
                    }
                }
            }
            closeList()
            return sb.toString().trim()
        }
    }
}
