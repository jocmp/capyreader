package com.capyreader.app.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler
import androidx.webkit.WebViewAssetLoader.ResourcesPathHandler
import coil.executeBlocking
import coil.imageLoader
import coil.request.ImageRequest
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.WebViewInterface
import com.capyreader.app.common.openLink
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    onUpdate: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {}
) {
    AndroidView(
        factory = { state.webView },
        modifier = modifier,
        update = onUpdate,
        onRelease = {
            onDispose(it)
        }
    )
}

/**
 * AccompanistWebViewClient
 *
 * A parent class implementation of WebViewClient that can be subclassed to add custom behaviour.
 *
 * As Accompanist Web needs to set its own web client to function, it provides this intermediary
 * class that can be overridden if further custom behaviour is required.
 */
open class AccompanistWebViewClient(private val assetLoader: WebViewAssetLoader) : WebViewClient(),
    KoinComponent {
    open lateinit var state: WebViewState
        internal set

    private val appPreferences by inject<AppPreferences>()

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        val accept = request.requestHeaders.getOrDefault("Accept", null)

        if (accept != null && accept.contains("image")) {
            try {
                val imageRequest = ImageRequest.Builder(view.context)
                    .data(request.url)
                    .build()
                val bitmap =
                    view.context.imageLoader.executeBlocking(imageRequest).drawable?.toBitmap()

                if (bitmap != null) {
                    return WebResourceResponse(
                        "image/jpg",
                        "UTF-8",
                        bitmapInputStream(bitmap)
                    )
                }
            } catch (exception: Exception) {
                return null
            }
        }

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

/**
 * A state holder to hold the state for the WebView. In most cases this will be remembered
 * using the rememberWebViewState(uri) function.
 */
@SuppressLint("SetJavaScriptEnabled")
@Stable
class WebViewState(
    context: Context,
    navigateToMedia: (url: String) -> Unit,
) {
    // We need access to this in the state saver. An internal DisposableEffect or AndroidView
    // onDestroy is called after the state saver and so can't be used.
    internal var webView by mutableStateOf<WebView>(
        WebView(context).apply {
            this.settings.javaScriptEnabled = true
            this.settings.mediaPlaybackRequiresUserGesture = false
            isVerticalScrollBarEnabled = false
            addJavascriptInterface(
                WebViewInterface(
                    navigateToMedia = navigateToMedia,
                ),
                WebViewInterface.INTERFACE_NAME
            )

            setBackgroundColor(context.getColor(android.R.color.transparent))

            webChromeClient = WebChromeClient()
            webViewClient = AccompanistWebViewClient(
                assetLoader =
                WebViewAssetLoader.Builder()
                    .setDomain("appassets.androidplatform.net")
                    .addPathHandler("/assets/", AssetsPathHandler(context))
                    .addPathHandler("/res/", ResourcesPathHandler(context))
                    .build()
            ).also {
                it.state = this@WebViewState
            }
        }
    )

    fun loadHtml(html: String) {
        webView.loadDataWithBaseURL(
            ASSET_BASE_URL,
            html,
            null,
            "utf-8",
            null
        )
    }

    fun clearView() {
        webView.apply {
            clearHistory()
            loadUrl("about:blank")
        }
    }
}

@Composable
fun rememberWebViewState(context: Context, navigateToMedia: (url: String) -> Unit) =
    remember { WebViewState(context, navigateToMedia) }

private fun bitmapInputStream(bitmap: Bitmap): InputStream {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    val bitmapData = byteArrayOutputStream.toByteArray()
    return ByteArrayInputStream(bitmapData)
}

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
private const val ASSET_BASE_URL = "https://appassets.androidplatform.net"
