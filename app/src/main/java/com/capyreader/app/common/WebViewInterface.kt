package com.capyreader.app.common

import android.content.Context
import android.webkit.JavascriptInterface
import coil.imageLoader
import coil.request.ImageRequest
import com.jocmp.capy.common.optionalURL

class WebViewInterface(
    private val navigateToMedia: (url: String) -> Unit,
) {
    @JavascriptInterface
    fun openImage(src: String) {
        optionalURL(src)?.let {
            navigateToMedia(it.toString())
        }
    }

    companion object {
        const val INTERFACE_NAME = "Android"
    }
}
