package com.capyreader.app.common

import android.webkit.JavascriptInterface
import com.jocmp.capy.common.optionalURL

class WebViewInterface(
    private val navigateToMedia: (media: Media) -> Unit,
) {
    @JavascriptInterface
    fun openImage(src: String, altText: String?) {
        optionalURL(src)?.let {
            navigateToMedia(Media(url = src, altText = altText))
        }
    }

    companion object {
        const val INTERFACE_NAME = "Android"
    }
}
