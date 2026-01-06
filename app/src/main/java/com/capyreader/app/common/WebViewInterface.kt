package com.capyreader.app.common

import android.webkit.JavascriptInterface
import com.capyreader.app.ui.components.ShareLink
import com.jocmp.capy.common.optionalURL
import com.jocmp.capy.logging.CapyLog
import kotlinx.serialization.json.Json

class WebViewInterface(
    private val navigateToMedia: (media: Media) -> Unit,
    private val onRequestLinkDialog: (link: ShareLink) -> Unit,
    private val onRequestImageDialog: (imageUrl: String) -> Unit = {},
    private val onOpenAudioPlayer: (audio: AudioEnclosure) -> Unit = {},
    private val onPauseAudio: () -> Unit = {},
) {
    var onRequestAudioState: () -> Unit = {}
    @JavascriptInterface
    fun openImageGallery(imagesJson: String, clickedIndex: Int) {
        try {
            val mediaItems = Json.decodeFromString<List<MediaItem>>(imagesJson)
                .filter { optionalURL(it.url) != null }

            if (mediaItems.isNotEmpty()) {
                val index = clickedIndex.coerceIn(0, mediaItems.size - 1)

                navigateToMedia(Media(images = mediaItems, startIndex = index))
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

    @JavascriptInterface
    fun showImageDialog(imageUrl: String) {
        optionalURL(imageUrl)?.let {
            onRequestImageDialog(it.toString())
        }
    }

    @JavascriptInterface
    fun openAudioPlayer(audioJson: String) {
        try {
            val audio = Json.decodeFromString<AudioEnclosure>(audioJson)
            onOpenAudioPlayer(audio)
        } catch (e: Exception) {
            CapyLog.error("open_audio_player", e)
        }
    }

    @JavascriptInterface
    fun pauseAudio() {
        onPauseAudio()
    }

    @JavascriptInterface
    fun requestAudioState() {
        onRequestAudioState()
    }

    companion object {
        const val INTERFACE_NAME = "Android"
    }
}
