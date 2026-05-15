package com.capyreader.app.ui.components

import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader.PathHandler
import com.jocmp.capy.logging.CapyLog
import java.io.File
import java.io.FileInputStream

class OfflineAssetPathHandler(private val rootDir: File) : PathHandler {
    override fun handle(path: String): WebResourceResponse? {
        return try {
            val file = File(rootDir, path).canonicalFile
            if (!file.canonicalPath.startsWith(rootDir.canonicalPath)) return null
            if (!file.exists() || file.isDirectory) return null
            WebResourceResponse(mimeFor(file.name), null, FileInputStream(file))
        } catch (e: Throwable) {
            CapyLog.error("offline_asset_handler", e)
            null
        }
    }

    private fun mimeFor(name: String): String =
        when (name.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "svg" -> "image/svg+xml"
            "mp4" -> "video/mp4"
            "webm" -> "video/webm"
            "mp3" -> "audio/mpeg"
            "ogg" -> "audio/ogg"
            else -> "application/octet-stream"
        }
}
