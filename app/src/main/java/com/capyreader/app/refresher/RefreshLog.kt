package com.capyreader.app.refresher

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
enum class RefreshEvent {
    STARTED,
    REFRESHED,
    SKIPPED_WIFI_ONLY,
    FAILED,
    STOPPED,
}

@Serializable
data class RefreshLogEntry(
    val timestamp: Long,
    val event: RefreshEvent,
    val notifications: Int? = null,
    val detail: String? = null,
)

class RefreshLog(
    private val file: File,
    private val limit: Int = LIMIT,
) {
    constructor(context: Context) : this(File(context.filesDir, FILE_NAME))

    private val mutex = Mutex()

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun record(
        event: RefreshEvent,
        notifications: Int? = null,
        detail: String? = null,
        timestamp: Long = System.currentTimeMillis(),
    ) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val entry = RefreshLogEntry(
                timestamp = timestamp,
                event = event,
                notifications = notifications,
                detail = detail,
            )
            val lines = readLines() + json.encodeToString(entry)

            file.writeText(lines.takeLast(limit).joinToString(separator = "\n", postfix = "\n"))
        }
    }

    suspend fun entries(): List<RefreshLogEntry> = withContext(Dispatchers.IO) {
        mutex.withLock {
            readLines().mapNotNull { line ->
                runCatching { json.decodeFromString<RefreshLogEntry>(line) }.getOrNull()
            }
        }
    }

    private fun readLines(): List<String> {
        if (!file.exists()) {
            return emptyList()
        }

        return file.readLines().filter { it.isNotBlank() }
    }

    companion object {
        const val FILE_NAME = "refresh_log.jsonl"
        const val LIMIT = 100
    }
}
