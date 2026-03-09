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

        // XHR/fetch from null origin (loadDataWithBaseURL)
        // Issue #1616
        val isCorsRequest = origin == "null" && url.startsWith("http")

        // iframe document load
        // Strips X-Frame-Options to allow embeds like Slashdot
        // Issue #1605
        val isIframeNavigation = !request.isForMainFrame &&
                accept?.startsWith("text/html") == true &&
                url.startsWith("http")

        // Sub-resource requests that need a Referer header for CDNs
        val isMediaRequest = pageUrl != null &&
                !request.isForMainFrame &&
                accept?.startsWith("text/html") != true &&
                url.startsWith("http")

        return isCorsRequest || isIframeNavigation || isMediaRequest
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
