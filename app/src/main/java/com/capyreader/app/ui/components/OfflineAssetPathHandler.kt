package com.capyreader.app.ui.components

import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader.PathHandler
import com.jocmp.capy.articles.OfflineAssetCache
import java.io.FileInputStream

class OfflineAssetPathHandler(
    private val offlineAssets: OfflineAssetCache,
) : PathHandler {
    override fun handle(path: String): WebResourceResponse? {
        val parts = path.trimStart('/').split('/', limit = 2)
        if (parts.size != 2) return null

        val articleID = parts[0]
        val fileName = parts[1]

        val file = offlineAssets.resolveAsset(articleID, fileName) ?: return null
        val contentType = offlineAssets.contentType(articleID, fileName)
            ?: "application/octet-stream"

        return WebResourceResponse(
            contentType,
            null,
            FileInputStream(file),
        )
    }
}
