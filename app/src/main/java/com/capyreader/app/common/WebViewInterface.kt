package com.capyreader.app.common

import android.webkit.JavascriptInterface
import com.capyreader.app.ui.components.ShareLink
import com.jocmp.capy.common.optionalURL
import com.jocmp.capy.logging.CapyLog
import kotlinx.serialization.json.Json

class WebViewInterface(
    private val navigateToMedia: (media: Media) -> Unit,
    private val onRequestLinkDialog: (link: ShareLink) -> Unit,
) {
    @JavascriptInterface
    fun openImageGallery(imagesJson: String, clickedIndex: Int) {
        try {
            val mediaItems = Json.decodeFromString<List<MediaItem>>(imagesJson)
                .filter { optionalURL(it.url) != null }
            if (mediaItems.isNotEmpty()) {
                navigateToMedia(Media(images = mediaItems, currentIndex = clickedIndex.coerceIn(0, mediaItems.size - 1)))
            }
        } catch (e: Exception) {
            CapyLog.error("open_image_gallery", e)
        }
    }

    @JavascriptInterface
    fun showLinkDialog(href: String, text: String) {
        optionalURL(href)?.let {
            onRequestLinkDialog(ShareLink(url = it.toString(), text = text.trimIndent()))
        }
    }

    companion object {
        const val INTERFACE_NAME = "Android"
    }
}
