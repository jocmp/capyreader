package com.capyreader.app.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import com.capyreader.app.ui.components.LoadingState.Finished
import com.capyreader.app.ui.components.LoadingState.Loading
import com.jocmp.capy.Article
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

/**
 * A wrapper around the Android View WebView to provide a basic WebView composable.
 *
 * If you require more customisation you are most likely better rolling your own and using this
 * wrapper as an example.
 *
 * The WebView attempts to set the layoutParams based on the Compose modifier passed in. If it
 * is incorrectly sizing, use the layoutParams composable function instead.
 *
 * @param state The webview state holder where the Uri to load is defined.
 * @param modifier A compose modifier
 * @param navigator An optional navigator object that can be used to control the WebView's
 * navigation from outside the composable.
 * @param onCreated Called when the WebView is first created, this can be used to set additional
 * settings on the WebView. WebChromeClient and WebViewClient should not be set here as they will be
 * subsequently overwritten after this lambda is called.
 * @param onDispose Called when the WebView is destroyed. Provides a bundle which can be saved
 * if you need to save and restore state in this WebView.
 * @param client Provides access to WebViewClient via subclassing
 * @param chromeClient Provides access to WebChromeClient via subclassing
 * @param factory An optional WebView factory for using a custom subclass of WebView
 */
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
        factory = { ctx ->
            WebView(ctx).apply {
                this.settings.javaScriptEnabled = true
                this.settings.mediaPlaybackRequiresUserGesture = false
                isVerticalScrollBarEnabled = false

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
        state.loadingState = Loading(0.0f)
        state.errorsForCurrentRequest.clear()
        state.pageTitle = null
        state.pageIcon = null
    }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        state.loadingState = Finished
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

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)

        if (error != null) {
            state.errorsForCurrentRequest.add(WebViewError(request, error))
        }
    }
}

/**
 * Sealed class for constraining possible loading states.
 * See [Loading] and [Finished].
 */
sealed class LoadingState {
    /**
     * Describes a WebView that has not yet loaded for the first time.
     */
    data object Initializing : LoadingState()

    /**
     * Describes a webview between `onPageStarted` and `onPageFinished` events, contains a
     * [progress] property which is updated by the webview.
     */
    data class Loading(val progress: Float) : LoadingState()

    /**
     * Describes a webview that has finished loading content.
     */
    data object Finished : LoadingState()
}

/**
 * A state holder to hold the state for the WebView. In most cases this will be remembered
 * using the rememberWebViewState(uri) function.
 */
@SuppressLint("SetJavaScriptEnabled")
@Stable
class WebViewState {
    /**
     * Whether the WebView is currently [LoadingState.Loading] data in its main frame (along with
     * progress) or the data loading has [LoadingState.Finished]. See [LoadingState]
     */
    public var loadingState: LoadingState by mutableStateOf(LoadingState.Initializing)
        internal set

    /**
     * Whether the webview is currently loading data in its main frame
     */
    val isLoading: Boolean
        get() = loadingState !is Finished

    /**
     * The title received from the loaded content of the current page
     */
    var pageTitle: String? by mutableStateOf(null)
        internal set

    /**
     * the favicon received from the loaded content of the current page
     */
    var pageIcon: Bitmap? by mutableStateOf(null)
        internal set

    /**
     * A list for errors captured in the last load. Reset when a new page is loaded.
     * Errors could be from any resource (iframe, image, etc.), not just for the main page.
     * For more fine grained control use the OnError callback of the WebView.
     */
    val errorsForCurrentRequest: SnapshotStateList<WebViewError> = mutableStateListOf()

    // We need access to this in the state saver. An internal DisposableEffect or AndroidView
    // onDestroy is called after the state saver and so can't be used.
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

@Stable
class WebViewNavigator(private val coroutineScope: CoroutineScope) {
    private sealed interface NavigationEvent {
        data object Back : NavigationEvent
        data object Forward : NavigationEvent
        data object Reload : NavigationEvent
        data object StopLoading : NavigationEvent

        data object ClearView : NavigationEvent

        data class LoadUrl(
            val url: String,
            val additionalHttpHeaders: Map<String, String> = emptyMap()
        ) : NavigationEvent

        data class LoadHtml(
            val html: String,
            val mimeType: String? = null,
            val encoding: String? = "utf-8",
            val historyUrl: String? = null
        ) : NavigationEvent

        data class PostUrl(
            val url: String,
            val postData: ByteArray
        ) : NavigationEvent {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as PostUrl

                if (url != other.url) return false
                return postData.contentEquals(other.postData)
            }

            override fun hashCode(): Int {
                var result = url.hashCode()
                result = 31 * result + postData.contentHashCode()
                return result
            }
        }
    }

    private val navigationEvents: MutableSharedFlow<NavigationEvent> = MutableSharedFlow(replay = 1)

    // Use Dispatchers.Main to ensure that the webview methods are called on UI thread
    @OptIn(ExperimentalCoroutinesApi::class)
    internal suspend fun WebView.handleNavigationEvents(): Nothing = withContext(Dispatchers.Main) {
        navigationEvents.collect { event ->
            when (event) {
                is NavigationEvent.Back -> {
                    goBack()
                }

                is NavigationEvent.Forward -> goForward()
                is NavigationEvent.Reload -> reload()
                is NavigationEvent.StopLoading -> stopLoading()
                is NavigationEvent.LoadHtml -> {
                    loadDataWithBaseURL(
                        ASSET_BASE_URL,
                        event.html,
                        event.mimeType,
                        event.encoding,
                        event.historyUrl
                    )
                }

                is NavigationEvent.ClearView -> {
                    navigationEvents.resetReplayCache()
                    clearHistory()
                    loadUrl("about:blank")
                }

                is NavigationEvent.LoadUrl -> {
                    loadUrl(event.url, event.additionalHttpHeaders)
                }

                is NavigationEvent.PostUrl -> {
                    postUrl(event.url, event.postData)
                }
            }
        }
    }

    fun loadHtml(
        html: String,
        mimeType: String? = null,
        encoding: String? = "utf-8",
        historyUrl: String? = null
    ) {
        coroutineScope.launch {
            navigationEvents.emit(
                NavigationEvent.LoadHtml(
                    html,
                    mimeType,
                    encoding,
                    historyUrl
                )
            )
        }
    }
}

/**
 * A wrapper class to hold errors from the WebView.
 */
@Immutable
data class WebViewError(
    /**
     * The request the error came from.
     */
    val request: WebResourceRequest?,
    /**
     * The error that was reported.
     */
    val error: WebResourceError
)

@Composable
fun rememberWebViewState(article: Article) = remember(article.id) {
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
