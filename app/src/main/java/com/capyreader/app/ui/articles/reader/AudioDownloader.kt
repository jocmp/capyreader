package com.capyreader.app.ui.articles.reader

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.webkit.URLUtil
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.jocmp.capy.logging.CapyLog

object AudioDownloader {
    fun download(url: String, title: String?, context: Context): Result<Long> {
        return try {
            val manager = context.getSystemService<DownloadManager>()
                ?: throw IllegalStateException("DownloadManager unavailable")

            val request = DownloadManager.Request(url.toUri())
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName(url = url, title = title),
                )

            if (title != null) {
                request.setTitle(title)
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
    internal fun fileName(url: String, title: String?): String {
        val guessed = URLUtil.guessFileName(url, null, null)
        val name = title
            ?.replace(INVALID_FILE_CHARS, " ")
            ?.replace(WHITESPACE, " ")
            ?.trim()
            ?.take(MAX_TITLE_LENGTH)
            ?.ifBlank { null }
            ?: return guessed

        val extension = guessed.substringAfterLast('.', missingDelimiterValue = "")

        if (extension.isEmpty()) {
            return name
        }

        return "$name.$extension"
    }

    private val INVALID_FILE_CHARS = Regex("""[\\/:*?"<>|\p{Cntrl}]""")
    private val WHITESPACE = Regex("""\s+""")
    private const val MAX_TITLE_LENGTH = 120
}
