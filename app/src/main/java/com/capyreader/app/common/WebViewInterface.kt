package com.capyreader.app.common

import android.webkit.JavascriptInterface
import com.capyreader.app.ui.components.ShareLink
import com.jocmp.capy.common.optionalURL

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
    fun showLinkDialog(href: String, text: String) {
        optionalURL(href)?.let {
            onRequestLinkDialog(ShareLink(url = it.toString(), text = text.trimIndent()))
        }
    }

    companion object {
        const val INTERFACE_NAME = "Android"
    }
}
