package com.capyreader.app.ui.articles.reader

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.webkit.URLUtil
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.jocmp.capy.logging.CapyLog

object AudioDownloader {
    fun download(
        url: String,
        title: String?,
        mimeType: String?,
        context: Context,
    ): Result<Long> {
        return try {
            val manager = context.getSystemService<DownloadManager>()
                ?: throw IllegalStateException("DownloadManager unavailable")

            val request = DownloadManager.Request(url.toUri())
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName(url = url, title = title, mimeType = mimeType),
                )

            if (title != null) {
                request.setTitle(title)
            }

            if (mimeType != null) {
                request.setMimeType(mimeType)
            }

            Result.success(manager.enqueue(request))
        } catch (e: Exception) {
            CapyLog.error("audio_download", error = e)
            Result.failure(e)
        }
    }

    /**
     * Podcast hosts often serve every episode under the same name
     * (e.g. NPR's "default.mp3"), so prefer the episode title.
     */
    internal fun fileName(url: String, title: String?, mimeType: String?): String {
        val guessed = URLUtil.guessFileName(url, null, mimeType)
        val name = title
            ?.replace(INVALID_FILE_CHARS, " ")
            ?.replace(WHITESPACE, " ")
            ?.trim()
            ?.trimStart('.')
            ?.let { truncateBytes(it, MAX_TITLE_BYTES) }
            ?.trim()
            ?.ifBlank { null }
            ?: return guessed

        val extension = guessed.substringAfterLast('.', missingDelimiterValue = "")

        if (extension.isEmpty()) {
            return name
        }

        return "$name.$extension"
    }

    /** Filenames are limited to 255 bytes, so cut on code points by UTF-8 size. */
    private fun truncateBytes(value: String, maxBytes: Int): String {
        val codePoints = value.codePoints().toArray().map { String(Character.toChars(it)) }
        val totals = codePoints
            .runningFold(0) { total, char -> total + char.toByteArray(Charsets.UTF_8).size }
            .drop(1)

        return codePoints
            .zip(totals)
            .takeWhile { (_, total) -> total <= maxBytes }
            .joinToString(separator = "") { (char, _) -> char }
    }

    private val INVALID_FILE_CHARS = Regex("""[\\/:*?"<>|\p{Cntrl}]""")
    private val WHITESPACE = Regex("""\s+""")
    private const val MAX_TITLE_BYTES = 200
}
