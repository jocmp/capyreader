package com.capyreader.app.ui.components

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebView.HitTestResult.SRC_ANCHOR_TYPE
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler
import androidx.webkit.WebViewAssetLoader.ResourcesPathHandler
import com.capyreader.app.common.Media
import com.capyreader.app.common.WebViewInterface
import com.capyreader.app.common.rememberTalkbackPreference
import com.capyreader.app.ui.articles.detail.articleTemplateColors
import com.capyreader.app.ui.articles.detail.byline
import com.jocmp.capy.Article
import com.jocmp.capy.articles.ArticleRenderer
import com.jocmp.capy.common.launchIO
import com.jocmp.capy.common.windowOrigin
import com.jocmp.capy.common.withUIContext
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.koinInject
import org.koin.core.component.KoinComponent

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebView(
    modifier: Modifier,
    state: WebViewState,
) {
    AndroidView(
        modifier = modifier,
        factory = { state.webView },
    )
}

class AccompanistWebViewClient(
    private val assetLoader: WebViewAssetLoader,
    private val onOpenLink: (url: Uri) -> Unit,
) : WebViewClient(),
    KoinComponent {
    lateinit var state: WebViewState
        internal set

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        val asset = assetLoader.shouldInterceptRequest(request.url) ?: return null

        val headers = asset.responseHeaders ?: mutableMapOf()
        headers["Access-Control-Allow-Origin"] = "*"
        asset.responseHeaders = headers

        return asset
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url

        if (url != null) {
            onOpenLink(url)
        }

        return true
    }
}

@Stable
class WebViewState(
    private val renderer: ArticleRenderer,
    private val colors: Map<String, String>,
    private val scope: CoroutineScope,
    private val enableNativeScroll: Boolean,
    internal val webView: WebView,
) {
    private var htmlId: String? = null

    init {
        loadEmpty()
    }

    fun loadHtml(article: Article, showImages: Boolean) {
        val id = article.id

        if (htmlId == null || id != htmlId) {
            webView.isVerticalScrollBarEnabled = enableNativeScroll
        }

        htmlId = id

        scope.launchIO {
            val html = renderer.render(
                article,
                hideImages = !showImages,
                byline = article.byline(context = webView.context),
                colors = colors
            )

            withUIContext {
                webView.loadDataWithBaseURL(
                    windowOrigin(article.url),
                    html,
                    null,
                    "UTF-8",
                    null,
                )
            }
        }
    }

    fun reset() {
        htmlId = null
        loadEmpty()
    }

    private fun loadEmpty() = webView.loadUrl("about:blank")
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun rememberWebViewState(
    renderer: ArticleRenderer = koinInject(),
    onNavigateToMedia: (media: Media) -> Unit,
    onRequestLinkDialog: (link: ShareLink) -> Unit,
    onOpenLink: (url: Uri) -> Unit,
    key: String? = null,
): WebViewState {
    val enableNativeScroll by rememberTalkbackPreference()
    val colors = articleTemplateColors()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val reset = if (enableNativeScroll) {
        key
    } else {
        null
    }

    val client = remember {
        AccompanistWebViewClient(
            assetLoader = WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", AssetsPathHandler(context))
                .addPathHandler("/res/", ResourcesPathHandler(context))
                .build(),
            onOpenLink = onOpenLink,
        )
    }

    return remember(reset, enableNativeScroll) {
        val webView = WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                mediaPlaybackRequiresUserGesture = false
                domStorageEnabled = true
            }
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false

            setOnLongClickListener {
                hitTestResult.type == SRC_ANCHOR_TYPE
            }

            addJavascriptInterface(
                WebViewInterface(
                    navigateToMedia = { onNavigateToMedia(it) },
                    onRequestLinkDialog = onRequestLinkDialog,
                ),
                WebViewInterface.INTERFACE_NAME
            )

            setBackgroundColor(context.getColor(android.R.color.transparent))

            webViewClient = client
        }

        WebViewState(
            renderer,
            colors,
            scope,
            enableNativeScroll = enableNativeScroll,
            webView,
        ).also {
            client.state = it
        }
    }
}
