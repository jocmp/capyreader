package com.capyreader.app.common

import android.content.Context
import android.net.Uri
import android.webkit.JavascriptInterface
import androidx.browser.customtabs.CustomTabsIntent
import com.jocmp.capy.common.optionalURL

class WebViewInterface(private val context: Context) {
    @JavascriptInterface
    fun openVideo(src: String) {
        optionalURL(src)?.let {
            CustomTabsIntent
                .Builder()
                .build()
                .launchUrl(context, Uri.parse(it.toString()))
        }
    }

    companion object {
        const val INTERFACE_NAME = "Android"
    }
}
