package com.capyreader.app.common

import android.content.Context
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.webkit.WebViewAssetLoader
import com.capyreader.app.ui.articles.detail.byline
import com.jocmp.capy.Article
import com.jocmp.capy.articles.ArticleRenderer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Helper class to print article content using Android's Print Framework.
 * Uses the same ArticleRenderer and styles as the article reader view.
 */
class ArticlePrintHelper(
    private val context: Context,
    private val article: Article,
) : KoinComponent {
    private val renderer: ArticleRenderer by inject()

    fun printArticle() {
        // Create asset loader for loading stylesheets and fonts
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
            .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(context))
            .build()

        // Create a WebView for printing
        val webView = WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = false
                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
        }
        
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                // Intercept asset requests to load local files
                val asset = assetLoader.shouldInterceptRequest(request.url)
                if (asset != null) {
                    val headers = asset.responseHeaders ?: mutableMapOf()
                    headers["Access-Control-Allow-Origin"] = "*"
                    asset.responseHeaders = headers
                    return asset
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageFinished(view: WebView, url: String) {
                createWebPrintJob(view)
            }
        }

        // Use the same renderer as the article reader with print-optimized colors
        val htmlContent = renderer.render(
            article = article,
            byline = article.byline(context),
            colors = getPrintColors(),
            hideImages = false,
        )

        webView.loadDataWithBaseURL(
            "https://appassets.androidplatform.net",
            htmlContent,
            "text/html",
            "UTF-8",
            null
        )
    }

    private fun createWebPrintJob(webView: WebView) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager

        // Create a print adapter from the WebView
        val printAdapter = webView.createPrintDocumentAdapter(article.title)

        // Create a print job with the name and adapter
        val jobName = "${context.packageName} - ${article.title}"

        // Pass null to use system defaults and let the user choose in the print dialog
        printManager.print(jobName, printAdapter, null)
    }

    companion object {
        /**
         * Returns colors optimized for printing (light background, dark text)
         */
        fun getPrintColors(): Map<String, String> {
            return mapOf(
                "color_primary" to toHex(Color.Black),
                "color_surface" to toHex(Color.White),
                "color_surface_container_highest" to toHex(Color(0xFFF5F5F5)),
                "color_on_surface" to toHex(Color.Black),
                "color_on_surface_variant" to toHex(Color(0xFF666666)),
                "color_surface_variant" to toHex(Color(0xFFEEEEEE)),
                "color_primary_container" to toHex(Color(0xFFF0F0F0)),
                "color_on_primary_container" to toHex(Color.Black),
                "color_secondary" to toHex(Color(0xFF444444)),
                "color_surface_container" to toHex(Color(0xFFFAFAFA)),
                "color_surface_tint" to toHex(Color(0xFFE0E0E0)),
            )
        }

        private fun toHex(color: Color): String {
            val argb = color.toArgb()
            return String.format("#%06X", 0xFFFFFF and argb)
        }
    }
}

/**
 * Extension function to make printing articles easier from a Context
 */
fun Context.printArticle(article: Article) {
    ArticlePrintHelper(this, article).printArticle()
}
