package com.capyreader.app.common

import android.webkit.JavascriptInterface
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
