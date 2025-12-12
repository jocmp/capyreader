package com.capyreader.app.common

import android.webkit.JavascriptInterface
import com.capyreader.app.ui.components.ShareLink
import com.jocmp.capy.common.optionalURL
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
data class ImageData(
    val src: String,
    val alt: String?
)

class WebViewInterface(
    private val navigateToMedia: (media: Media) -> Unit,
    private val onRequestLinkDialog: (link: ShareLink) -> Unit,
) {
    @JavascriptInterface
    fun openImage(src: String, altText: String?) {
        optionalURL(src)?.let {
            navigateToMedia(Media(url = src, altText = altText))
        }
    }

    @JavascriptInterface
    fun openImageGallery(imagesJson: String, clickedIndex: Int) {
        try {
            val imageDataList = Json.decodeFromString<List<ImageData>>(imagesJson)
            val mediaItems = imageDataList.mapNotNull { imageData ->
                optionalURL(imageData.src)?.let {
                    MediaItem(url = imageData.src, altText = imageData.alt)
                }
            }
            if (mediaItems.isNotEmpty()) {
                navigateToMedia(Media(images = mediaItems, currentIndex = clickedIndex.coerceIn(0, mediaItems.size - 1)))
            }
        } catch (e: Exception) {
            // Fallback to single image
            // This shouldn't happen, but just in case
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
