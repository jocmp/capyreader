package com.capyreader.app.ui.components

import android.webkit.WebResourceRequest
import java.net.URI

object WebRequestProxyPolicy {
    fun shouldProxy(url: String, request: WebResourceRequest, pageUrl: String?): Boolean {
        val origin = request.requestHeaders["Origin"]
        val accept = request.requestHeaders["Accept"]

        if (isKnownHTMLRedirect(url)) {
            return false
        }

        // XHR/fetch from null origin (loadDataWithBaseURL). Excludes image/video/audio Accept
        // types so <img> sub-resources - which also carry Origin: null from the same
        // null-baseURL page - aren't swept into this and proxied like a real XHR/fetch call.
        // Issue #1616
        val isMediaAccept = accept?.let {
            it.startsWith("image/") || it.startsWith("video/") || it.startsWith("audio/")
        } == true
        val isCorsRequest = origin == "null" && url.startsWith("http") && !isMediaAccept

        // iframe document load
        // Strips X-Frame-Options to allow embeds like Slashdot
        // Issue #1605
        val isIframeNavigation = !request.isForMainFrame &&
                accept?.startsWith("text/html") == true &&
                url.startsWith("http")

        // Image/media sub-resource proxying (Referer headers for CDNs, Issue #1878) is disabled:
        // it forced every image through a synchronous OkHttp call in shouldInterceptRequest,
        // serializing image loads on the WebView's request thread. It also happened to warm
        // Coil's shared cache for the media viewer, which needs a non-blocking replacement.

        return isCorsRequest || isIframeNavigation
    }

    // Reddit embeds www.reddit.com/media?url=... as image srcs in feeds,
    // but that endpoint serves an HTML viewer page, not the image itself.
    // Issue #1888
    internal fun isKnownHTMLRedirect(url: String): Boolean {
        return try {
            val uri = URI(url)
            val host = uri.host ?: return false
            (host.endsWith("reddit.com") && uri.path == "/media") ||
                    host.endsWith("preview.redd.it")
        } catch (_: Exception) {
            false
        }
    }
}
