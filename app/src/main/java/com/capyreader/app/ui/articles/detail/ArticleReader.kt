package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import com.capyreader.app.common.AudioEnclosure
import com.capyreader.app.common.Media
import com.capyreader.app.common.rememberTalkbackPreference
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.ReaderImageVisibility
import com.capyreader.app.ui.ConnectivityType
import com.capyreader.app.ui.LocalConnectivity
import com.capyreader.app.ui.LocalLinkOpener
import com.capyreader.app.ui.articles.ColumnScrollbar
import com.capyreader.app.ui.components.WebView
import com.capyreader.app.ui.components.WebViewState
import com.capyreader.app.ui.components.rememberSaveableShareLink
import com.capyreader.app.ui.components.rememberWebViewState
import com.jocmp.capy.Article
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@Composable
fun ArticleReader(
    article: Article,
    onSelectMedia: (media: Media) -> Unit,
    onSelectAudio: (audio: AudioEnclosure) -> Unit = {},
    onPauseAudio: () -> Unit = {},
    currentAudioUrl: String? = null,
    isAudioPlaying: Boolean = false,
) {
    val (shareLink, setShareLink) = rememberSaveableShareLink()
    val linkOpener = LocalLinkOpener.current

    val webViewState = rememberWebViewState(
        key = article.id,
        onNavigateToMedia = onSelectMedia,
        onRequestLinkDialog = { setShareLink(it) },
        onOpenLink = { linkOpener.open(it) },
        onOpenAudioPlayer = onSelectAudio,
        onPauseAudio = onPauseAudio,
        currentAudioUrl = currentAudioUrl,
        isAudioPlaying = isAudioPlaying,
    )

    LaunchedEffect(currentAudioUrl, isAudioPlaying) {
        webViewState.updateAudioPlayState(currentAudioUrl, isAudioPlaying)
    }

    val showImages = rememberImageVisibility()
    val improveTalkback by rememberTalkbackPreference()

    if (improveTalkback) {
        Column(
            Modifier.fillMaxSize()
        ) {
            WebView(
                modifier = Modifier.fillMaxSize(),
                state = webViewState,
                article = article,
                showImages = showImages,
            )
        }
    } else {
        ScrollableWebView(webViewState, article, showImages)
    }

    ArticleStyleListener(webView = webViewState.webView)

    if (shareLink != null) {
        ShareLinkDialog(
            onClose = {
                setShareLink(null)
            },
            link = shareLink,
        )
    }
}

@Composable
fun ScrollableWebView(webViewState: WebViewState, article: Article, showImages: Boolean) {
    var maxHeight by remember { mutableFloatStateOf(0f) }
    val scrollState = rememberSaveable(article.id, saver = ScrollState.Saver) {
        ScrollState(initial = 0)
    }

    var lastScrollYPercent by rememberSaveable(article.id) { mutableFloatStateOf(0f) }

    CornerTapGestureScroll(
        maxArticleHeight = maxHeight,
        scrollState = scrollState,
    ) {
        ColumnScrollbar(state = scrollState) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .onGloballyPositioned { coordinates ->
                        maxHeight = coordinates.size.height.toFloat()
                    }
            ) {
                WebView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    state = webViewState,
                    article = article,
                    showImages = showImages,
                )
            }
        }
    }

    LaunchedEffect(scrollState.value, maxHeight) {
        if (scrollState.value > 0 && maxHeight > 0) {
            lastScrollYPercent = scrollState.value / maxHeight
        }
    }
    LaunchedEffect(scrollState.maxValue, maxHeight) {
        if (scrollState.maxValue > 0 && maxHeight > 0) {
            scrollState.scrollTo((lastScrollYPercent * maxHeight).roundToInt())
        }
    }
}

@Composable
fun rememberImageVisibility(appPreferences: AppPreferences = koinInject()): Boolean {
    val imagePreference by appPreferences.readerOptions
        .imageVisibility
        .changes()
        .collectAsState(appPreferences.readerOptions.imageVisibility.get())

    val connectivity = LocalConnectivity.current

    return imagePreference == ReaderImageVisibility.ALWAYS_SHOW ||
            (imagePreference == ReaderImageVisibility.SHOW_ON_WIFI && connectivity.isOnWifi)
}

private val ConnectivityType.isOnWifi
    get() = this == ConnectivityType.WIFI || this == ConnectivityType.ETHERNET
