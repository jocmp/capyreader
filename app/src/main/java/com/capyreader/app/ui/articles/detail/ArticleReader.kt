package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.Media
import com.capyreader.app.common.ReaderImageVisibility
import com.capyreader.app.ui.ConnectivityType
import com.capyreader.app.ui.LocalConnectivity
import com.capyreader.app.ui.articles.ColumnScrollbar
import com.jocmp.capy.Article
import com.jocmp.capy.articles.ArticleRenderer
import com.jocmp.capy.common.windowOrigin
import org.koin.compose.koinInject

@Composable
fun ArticleReader(
    article: Article,
    onNavigateToMedia: (media: Media) -> Unit,
    renderer: ArticleRenderer = koinInject(),
) {
    val context = LocalContext.current
    val colors = articleTemplateColors()
    val showImages = rememberImageVisibility()
    var maxHeight by remember { mutableFloatStateOf(0f) }
    val scrollState = rememberScrollState()

    var lastScrollY by rememberSaveable { mutableIntStateOf(0) }

    val html = remember(article.id, article.content) {
        renderer.render(
            article,
            hideImages = !showImages,
            byline = article.byline(context = context),
            colors = colors
        )
    }

    ReaderPagingBox(
        maxArticleHeight = maxHeight,
        scrollState = scrollState,
    ) {
        ColumnScrollbar(state = scrollState) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .verticalScroll(scrollState)
                    .onGloballyPositioned { coordinates ->
                        if (maxHeight == 0f) {
                            maxHeight = coordinates.size.height.toFloat()
                        }
                    }
            ) {
                ContentWebView(
                    html = html,
                    origin = windowOrigin(article.url),
                    onNavigateToMedia = onNavigateToMedia,
                    onRelease = {
                        lastScrollY = scrollState.value
                    }
                )
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }

    LaunchedEffect(lastScrollY, scrollState.maxValue) {
        if (scrollState.maxValue > 0 && lastScrollY > 0) {
            scrollState.scrollTo(lastScrollY)
            lastScrollY = 0
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
