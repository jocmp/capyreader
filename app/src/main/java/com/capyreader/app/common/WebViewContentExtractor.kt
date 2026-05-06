package com.capyreader.app.common

import android.annotation.SuppressLint
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
import com.jocmp.capy.articles.ContentExtractor
import com.jocmp.capy.logging.CapyLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

private const val EXTRACT_TIMEOUT_MS = 20_000L
private const val BRIDGE_NAME = "AndroidExtractor"
private const val BASE_URL = "https://appassets.androidplatform.net/"

class WebViewContentExtractor(private val application: Application) : ContentExtractor {
    override suspend fun extract(url: String?, html: String): Result<String> {
        return withTimeoutOrNull(EXTRACT_TIMEOUT_MS) {
            withContext(Dispatchers.Main) { runExtraction(url, html) }
        } ?: Result.failure(Throwable("Mercury extraction timed out"))
    }

    @SuppressLint("SetJavaScriptEnabled")
    private suspend fun runExtraction(url: String?, html: String): Result<String> =
        suspendCancellableCoroutine { cont ->
            val webView = WebView(application).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
            }

            val assetLoader = WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(application))
                .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(application))
                .build()

            val resumed = AtomicBoolean(false)

            fun finish(result: Result<String>) {
                if (!resumed.compareAndSet(false, true)) return
                Handler(Looper.getMainLooper()).post { webView.destroy() }
                if (cont.isActive) cont.resume(result)
            }

            webView.webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)

                override fun onPageFinished(view: WebView, finishedUrl: String) {
                    val js = """
                        (async () => {
                          try {
                            const r = await Mercury.parse(${jsLiteral(url.orEmpty())}, { html: ${jsLiteral(html)} });
                            if (r && r.content) {
                              $BRIDGE_NAME.onResult(r.content);
                            } else {
                              $BRIDGE_NAME.onError("Mercury returned no content");
                            }
                          } catch (e) {
                            $BRIDGE_NAME.onError(String(e && e.message ? e.message : e));
                          }
                        })();
                    """.trimIndent()
                    view.evaluateJavascript(js, null)
                }
            }

            webView.addJavascriptInterface(object {
                @JavascriptInterface
                fun onResult(content: String) {
                    if (content.isBlank()) {
                        finish(Result.failure(Throwable("Empty content")))
                    } else {
                        finish(Result.success(content))
                    }
                }

                @JavascriptInterface
                fun onError(message: String) {
                    CapyLog.warn("offline_extract_js_error", mapOf("error" to message))
                    finish(Result.failure(Throwable(message)))
                }
            }, BRIDGE_NAME)

            cont.invokeOnCancellation {
                if (resumed.compareAndSet(false, true)) {
                    Handler(Looper.getMainLooper()).post { webView.destroy() }
                }
            }

            val shell = """
                <!DOCTYPE html><html><head>
                <meta charset="utf-8">
                <script src="${BASE_URL}assets/mercury-parser.js"></script>
                </head><body></body></html>
            """.trimIndent()

            webView.loadDataWithBaseURL(BASE_URL, shell, "text/html", "UTF-8", null)
        }

    private fun jsLiteral(s: String): String {
        val sb = StringBuilder("\"")
        for (c in s) {
            when (c) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                '<' -> sb.append("\\u003c")
                '>' -> sb.append("\\u003e")
                '&' -> sb.append("\\u0026")
                ' ' -> sb.append("\\u2028")
                ' ' -> sb.append("\\u2029")
                else -> if (c.code < 0x20) {
                    sb.append("\\u%04x".format(c.code))
                } else {
                    sb.append(c)
                }
            }
        }
        sb.append("\"")
        return sb.toString()
    }
}
