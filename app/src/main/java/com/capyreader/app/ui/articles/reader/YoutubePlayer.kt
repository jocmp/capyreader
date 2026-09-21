package com.capyreader.app.ui.articles.reader

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.jocmp.mallet.LinearVideoSource

private const val REFERRER_URL = "https://capyreader.com"

private val YOUTUBE_WATCH = Regex("//www\\.youtube\\.com/watch\\?v=([A-Za-z0-9_-]+)$")

val LinearVideoSource.youtubeVideoID: String?
    get() = YOUTUBE_WATCH.find(link)?.groupValues?.get(1)

private class FullscreenVideo(
    val view: View,
    val callback: WebChromeClient.CustomViewCallback,
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YoutubePlayer(
    videoID: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val fullscreen = remember { mutableStateOf<FullscreenVideo?>(null) }

    val webView = remember(videoID) {
        WebView(context).apply {
            setBackgroundColor(Color.BLACK)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            webViewClient = WebViewClient()
            webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                    fullscreen.value = FullscreenVideo(view = view, callback = callback)
                }

                override fun onHideCustomView() {
                    fullscreen.value = null
                }
            }

            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )

            loadDataWithBaseURL(
                REFERRER_URL,
                embedDocument(videoID),
                "text/html",
                "utf-8",
                null,
            )
        }
    }

    DisposableEffect(webView) {
        onDispose {
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView.destroy()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier,
    )

    fullscreen.value?.let { video ->
        FullscreenVideoDialog(
            video = video,
            onDismissRequest = { video.callback.onCustomViewHidden() },
        )
    }
}

@Composable
private fun FullscreenVideoDialog(
    video: FullscreenVideo,
    onDismissRequest: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window

        LaunchedEffect(dialogWindow) {
            if (dialogWindow != null) {
                WindowCompat.setDecorFitsSystemWindows(dialogWindow, false)

                val insets = WindowCompat.getInsetsController(dialogWindow, dialogWindow.decorView)
                insets.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insets.hide(WindowInsetsCompat.Type.systemBars())
            }
        }

        AndroidView(
            factory = { context ->
                FrameLayout(context).apply { setBackgroundColor(Color.BLACK) }
            },
            update = { container ->
                if (video.view.parent !== container) {
                    (video.view.parent as? ViewGroup)?.removeView(video.view)

                    container.addView(
                        video.view,
                        FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        ),
                    )
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private fun embedDocument(videoID: String): String {
    return """
        <!DOCTYPE html>
        <html>
          <head>
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <style>
              html, body { margin: 0; padding: 0; background: black; overflow: hidden }
              iframe { border: 0; display: block; width: 100vw; aspect-ratio: 16 / 9 }
            </style>
          </head>
          <body>
            <iframe
              src="https://www.youtube-nocookie.com/embed/${videoID}?autoplay=1&playsinline=1"
              referrerpolicy="strict-origin-when-cross-origin"
              allow="autoplay; encrypted-media; picture-in-picture; fullscreen"
              allowfullscreen></iframe>
          </body>
        </html>
    """.trimIndent()
}
