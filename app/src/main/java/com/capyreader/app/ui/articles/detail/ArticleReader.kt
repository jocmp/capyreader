package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
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
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.Media
import com.capyreader.app.common.ReaderImageVisibility
import com.capyreader.app.ui.ConnectivityType
import com.capyreader.app.ui.LocalConnectivity
import com.capyreader.app.ui.articles.ColumnScrollbar
import com.capyreader.app.ui.components.WebView
import com.capyreader.app.ui.components.rememberWebViewState
import com.jocmp.capy.Article
import org.koin.compose.koinInject

@Composable
fun ArticleReader(
    article: Article,
    onNavigateToMedia: (media: Media) -> Unit,
) {
    val showImages = rememberImageVisibility()
    var maxHeight by remember { mutableFloatStateOf(0f) }
    val scrollState = rememberSaveable(saver = ScrollState.Saver) {
        ScrollState(initial = 0)
    }

    var lastScrollY by rememberSaveable { mutableIntStateOf(0) }

    val webViewState = rememberWebViewState(
        onNavigateToMedia = onNavigateToMedia,
    )

    ReaderPagingBox(
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
                    state = webViewState,
                    onDispose = {
                        lastScrollY = scrollState.value
                    },
                )
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }

    LaunchedEffect(article.id, article.content) {
        webViewState.loadHtml(article, showImages = showImages)
    }

    LaunchedEffect(lastScrollY, scrollState.maxValue) {
        if (scrollState.maxValue > 0 && lastScrollY > 0) {
            scrollState.scrollTo(lastScrollY)
            lastScrollY = 0
        }
    }

    ArticleStyleListener(webView = webViewState.webView)
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
