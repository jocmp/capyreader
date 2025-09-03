package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
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

@Composable
fun ArticleReader(
    article: Article,
    onSelectMedia: (media: Media) -> Unit,
) {
    val (shareLink, setShareLink) = rememberSaveableShareLink()
    val linkOpener = LocalLinkOpener.current

    val webViewState = rememberWebViewState(
        key = article.id,
        onNavigateToMedia = onSelectMedia,
        onRequestLinkDialog = { setShareLink(it) },
        onOpenLink = { linkOpener.open(it) }
    )

    val showImages = rememberImageVisibility()
    val improveTalkback by rememberTalkbackPreference()

    if (improveTalkback) {
        Column(
            Modifier.fillMaxSize()
        ) {
            WebView(
                modifier = Modifier.fillMaxSize(),
                state = webViewState,
            )
        }
    } else {
        ScrollableWebView(webViewState)
    }

    LaunchedEffect(article.id, article.content) {
        webViewState.loadHtml(article, showImages = showImages)
    }

    ArticleStyleListener(webView = webViewState.webView)

    DisposableEffect(article.id) {
        onDispose {
            webViewState.reset()
        }
    }

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
fun ScrollableWebView(webViewState: WebViewState) {
    var maxHeight by remember { mutableFloatStateOf(0f) }
    val scrollState = rememberSaveable(saver = ScrollState.Saver) {
        ScrollState(initial = 0)
    }

    var lastScrollY by rememberSaveable { mutableIntStateOf(0) }

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
                )
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }

    LaunchedEffect(scrollState.value) {
        if (scrollState.value > 0) {
            lastScrollY = scrollState.value
        }
    }
    LaunchedEffect(scrollState.maxValue) {
        if (scrollState.maxValue > 0) {
            scrollState.scrollTo(lastScrollY)
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
