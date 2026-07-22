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

        // Images load natively (fast, async, parallel) by default. Only a retry - marked by
        // media.js after a real <img> load failure, e.g. a CDN needing a Referer header
        // (Issue #1878) - goes through this proxy. Proxying every image up front serialized
        // them all through this synchronous call, which is what previously made pagination slow.
        return isCorsRequest || isIframeNavigation || retriedMediaFetch(url)
    }

    const val RETRY_QUERY_PARAM = "__capy_retry"

    private fun retriedMediaFetch(url: String): Boolean {
        return try {
            URI(url).query
                ?.split("&")
                ?.firstOrNull { it.startsWith("$RETRY_QUERY_PARAM=") }
                ?.substringAfter("=")
                ?.toBoolean() == true
        } catch (_: Exception) {
            false
        }
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
