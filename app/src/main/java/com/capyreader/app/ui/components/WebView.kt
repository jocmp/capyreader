package com.capyreader.app.ui.components

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler
import androidx.webkit.WebViewAssetLoader.DEFAULT_DOMAIN
import androidx.webkit.WebViewAssetLoader.ResourcesPathHandler
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.WebViewInterface
import com.capyreader.app.common.openLink
import com.capyreader.app.ui.articles.detail.articleTemplateColors
import com.capyreader.app.ui.articles.detail.byline
import com.jocmp.capy.Article
import com.jocmp.capy.articles.ArticleRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Doesn't really fetch from androidplatform.net. This is used as a placeholder domain:
 *
 * > Using http(s):// URLs to access local resources may conflict
 * > with a real website. This means that local files should only
 * > be hosted on domains your organization owns (at paths reserved for this purpose)
 * > or the default domain reserved for this: appassets.androidplatform.net
 *
 * * [How-to docs](https://developer.android.com/develop/ui/views/layout/webapps/load-local-content#mix-content)
 * * [JavaDoc](https://developer.android.com/reference/androidx/webkit/WebViewAssetLoader)
 */
private const val ASSET_BASE_URL = "https://${DEFAULT_DOMAIN}"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebView(
    state: WebViewState,
    onDispose: (WebView) -> Unit,
) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        factory = { state.webView },
        onRelease = {
            onDispose(it)
        }
    )
}

class AccompanistWebViewClient(
    private val assetLoader: WebViewAssetLoader,
) : WebViewClient(),
    KoinComponent {
    lateinit var state: WebViewState
        internal set

    private val appPreferences by inject<AppPreferences>()

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(request.url)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url

        if (view != null && url != null) {
            view.context.openLink(url, appPreferences)
        }

        return true
    }
}

@Stable
class WebViewState(
    private val renderer: ArticleRenderer,
    private val colors: Map<String, String>,
    private val scope: CoroutineScope,
    internal val webView: WebView,
) {
    private var htmlId: String? = null

    init {
        loadEmpty()
    }

    fun loadHtml(article: Article) {
        val id = article.id

        scope.launch {
            withContext(Dispatchers.IO) {
                val html = renderer.render(
                    article,
                    byline = article.byline(context = webView.context),
                    colors = colors
                )

                withContext(Dispatchers.Main) {
                    webView.loadDataWithBaseURL(
                        null,
                        html,
                        null,
                        "UTF-8",
                        null
                    )
                }
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
    onNavigateToMedia: (url: String) -> Unit,
): WebViewState {
    val colors = articleTemplateColors()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val client = remember {
        AccompanistWebViewClient(
            assetLoader = WebViewAssetLoader.Builder()
                .setDomain(DEFAULT_DOMAIN)
                .addPathHandler("/assets/", AssetsPathHandler(context))
                .addPathHandler("/res/", ResourcesPathHandler(context))
                .build(),
        )
    }

    return remember {
        val webView = WebView(context).apply {
            this.settings.javaScriptEnabled = true
            this.settings.mediaPlaybackRequiresUserGesture = false
            this.settings.offscreenPreRaster = true
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false

            addJavascriptInterface(
                WebViewInterface(
                    navigateToMedia = { onNavigateToMedia(it) }
                ),
                WebViewInterface.INTERFACE_NAME
            )

            setBackgroundColor(context.getColor(android.R.color.transparent))

            webViewClient = client
        }

        WebViewState(renderer, colors, scope, webView).also {
            client.state = it
        }
    }
}
