package com.capyreader.app.ui.components

import android.net.Uri
import android.webkit.WebResourceRequest
import com.capyreader.app.ui.components.WebRequestProxyPolicy.isKnownHTMLRedirect
import com.capyreader.app.ui.components.WebRequestProxyPolicy.shouldProxy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebRequestProxyPolicyTest {
    @Test
    fun shouldProxy_corsRequest() {
        assertTrue(
            shouldProxy(
                "https://example.com/feed.json",
                FakeWebResourceRequest(origin = "null"),
                pageUrl = null,
            )
        )
    }

    @Test
    fun shouldProxy_iframeNavigation() {
        assertTrue(
            shouldProxy(
                "https://slashdot.org/story/123",
                FakeWebResourceRequest(accept = "text/html,application/xhtml+xml"),
                pageUrl = "https://example.com",
            )
        )
    }

    @Test
    fun shouldProxy_mediaRequest() {
        assertTrue(
            shouldProxy(
                "https://cdn.example.com/image.jpg",
                FakeWebResourceRequest(accept = "image/webp,image/apng,*/*;q=0.8", origin = "null"),
                pageUrl = "https://example.com/article",
            )
        )
    }

    @Test
    fun shouldProxy_mainFrameIsSkipped() {
        assertFalse(
            shouldProxy(
                "https://example.com/",
                FakeWebResourceRequest(forMainFrame = true),
                pageUrl = null,
            )
        )
    }

    @Test
    fun shouldProxy_iframeSubResourceIsSkipped() {
        assertFalse(
            shouldProxy(
                "https://cdn.iframe-origin.com/script.js",
                FakeWebResourceRequest(
                    accept = "application/javascript",
                    origin = "https://iframe-origin.com",
                ),
                pageUrl = "https://example.com/article",
            )
        )
    }

    @Test
    fun shouldProxy_mediaRequestWithoutPageUrl() {
        assertFalse(
            shouldProxy(
                "https://cdn.example.com/image.jpg",
                FakeWebResourceRequest(accept = "image/webp"),
                pageUrl = null,
            )
        )
    }

    @Test
    fun shouldProxy_redditMediaEndpointIsSkipped() {
        assertFalse(
            shouldProxy(
                "https://www.reddit.com/media?url=https%3A%2F%2Fpreview.redd.it%2Fimage.jpg",
                FakeWebResourceRequest(accept = "text/html,application/xhtml+xml,*/*;q=0.8"),
                pageUrl = "https://www.reddit.com/r/sub/comments/abc/post/",
            )
        )
    }

    @Test
    fun isKnownHTMLRedirect_redditMedia() {
        assertTrue(isKnownHTMLRedirect("https://www.reddit.com/media?url=https%3A%2F%2Fpreview.redd.it%2Fimage.jpg"))
    }

    @Test
    fun isKnownHTMLRedirect_redditSubdomain() {
        assertTrue(isKnownHTMLRedirect("https://old.reddit.com/media?url=https%3A%2F%2Fpreview.redd.it%2Fimage.jpg"))
    }

    @Test
    fun isKnownHTMLRedirect_previewReddIt() {
        assertTrue(isKnownHTMLRedirect("https://preview.redd.it/9vuepbrvk0og1.jpeg?width=640&crop=smart&auto=webp&s=abc123"))
    }

    @Test
    fun isKnownHTMLRedirect_nonReddit() {
        assertFalse(isKnownHTMLRedirect("https://example.com/image.jpg"))
    }

    @Test
    fun isKnownHTMLRedirect_redditNonMediaPath() {
        assertFalse(isKnownHTMLRedirect("https://www.reddit.com/r/sub/comments/abc/"))
    }
}

private class FakeWebResourceRequest(
    private val accept: String? = null,
    private val origin: String? = null,
    private val forMainFrame: Boolean = false,
) : WebResourceRequest {
    override fun getUrl(): Uri = throw NotImplementedError()
    override fun getRequestHeaders() = buildMap {
        accept?.let { put("Accept", it) }
        origin?.let { put("Origin", it) }
    }
    override fun isForMainFrame() = forMainFrame
    override fun isRedirect() = false
    override fun hasGesture() = false
    override fun getMethod() = "GET"
}
