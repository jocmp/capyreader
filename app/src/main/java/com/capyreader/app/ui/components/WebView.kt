package com.capyreader.app.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup.*
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebView(
    state: WebViewState,
    onNavigateToMedia: (url: String) -> Unit,
    onDispose: (WebView) -> Unit,
) {
    val context = LocalContext.current

    val client = remember {
        AccompanistWebViewClient(
            assetLoader =
            WebViewAssetLoader.Builder()
                .setDomain("appassets.androidplatform.net")
                .addPathHandler("/assets/", AssetsPathHandler(context))
                .addPathHandler("/res/", ResourcesPathHandler(context))
                .build()
        )
    }
    client.state = state

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        factory = { ctx ->
            WebView(ctx).apply {
                this.settings.javaScriptEnabled = true
                this.settings.mediaPlaybackRequiresUserGesture = false
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false

                addJavascriptInterface(
                    WebViewInterface(
                        navigateToMedia = { onNavigateToMedia(it) },
                    ),
                    WebViewInterface.INTERFACE_NAME
                )

                setBackgroundColor(context.getColor(android.R.color.transparent))

                webViewClient = client
            }.also {
                state.webView = it
            }
        },
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

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
    }

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
                        bitmapInputStream(bitmap, Bitmap.CompressFormat.JPEG)
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

@Stable
class WebViewState {
    internal var webView by mutableStateOf<WebView?>(null)

    fun loadHtml(html: String) {
        webView?.loadDataWithBaseURL(
            ASSET_BASE_URL,
            html,
            null,
            "UTF-8",
            null,
        )
    }
}

@Composable
fun rememberWebViewState() = remember {
    WebViewState()
}

private fun bitmapInputStream(
    bitmap: Bitmap,
    compressFormat: Bitmap.CompressFormat
): InputStream {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(compressFormat, 100, byteArrayOutputStream)
    val bitmapData = byteArrayOutputStream.toByteArray()
    return ByteArrayInputStream(bitmapData)
}
