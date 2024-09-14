package com.capyreader.app.ui.articles.detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.components.WebView
import com.capyreader.app.ui.components.rememberSaveableWebViewState
import com.jocmp.capy.Article
import com.jocmp.capy.articles.ArticleRenderer
import my.nanihadesuka.compose.ColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleView(
    article: Article,
    renderer: ArticleRenderer = koinInject(),
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    onNavigateToMedia: (url: String) -> Unit,
    enableBackHandler: Boolean = false
) {
    val articleID = article.id
    val templateColors = articleTemplateColors()
    val colors = templateColors.asMap()
    val webViewState = rememberSaveableWebViewState(key = articleID)
    val byline = article.byline(context = LocalContext.current)
    val lastScrollY = rememberSaveable(articleID) {
        mutableIntStateOf(0)
    }
    val scrollState = rememberSaveable(articleID, saver = ScrollState.Saver) {
        ScrollState(0)
    }
    val showTopBar = scrollState.value == 0 || scrollState.lastScrolledBackward

    val extractedContentState = rememberExtractedContent(
        article = article,
        onComplete = { content ->
            article.let {
                webViewState.loadHtml(
                    renderer.render(
                        article,
                        byline = byline,
                        extractedContent = content,
                        colors = colors
                    )
                )
            }
        }
    )

    val extractedContent = extractedContentState.content

    fun onToggleExtractContent() {
        if (extractedContent.isComplete) {
            webViewState.loadHtml(renderer.render(article, byline = byline, colors = colors))
            extractedContentState.reset()
        } else if (!extractedContent.isLoading) {
            extractedContentState.fetch()
        }
    }

    Scaffold { innerPadding ->
        Box(Modifier.fillMaxSize()) {
            ColumnScrollbar(
                state = scrollState,
                settings = ScrollbarSettings.Default.copy(
                    thumbThickness = 4.dp,
                    hideDisplacement = 0.dp,
                    scrollbarPadding = 2.dp,
                    thumbSelectedColor = colorScheme.onSurfaceVariant,
                    thumbUnselectedColor = colorScheme.onSurfaceVariant
                )
            ) {
                Column(
                    Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Spacer(Modifier.height(TopAppBarDefaults.TopAppBarExpandedHeight))
                    WebView(
                        state = webViewState,
                        onNavigateToMedia = onNavigateToMedia,
                        onDispose = {
                            lastScrollY.intValue = scrollState.value
                        }
                    )
                }
            }
            AnimatedVisibility(
                visible = showTopBar,
                enter = fadeIn() + expandVertically(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ArticleTopBar(
                    article = article,
                    extractedContent = extractedContent,
                    onToggleExtractContent = ::onToggleExtractContent,
                    onToggleRead = onToggleRead,
                    onToggleStar = onToggleStar,
                    onClose = onBackPressed
                )
            }
        }
    }

    BackHandler(enableBackHandler) {
        onBackPressed()
    }

    LaunchedEffect(articleID) {
        if (extractedContent.requestShow) {
            extractedContentState.fetch()
        } else {
            val rendered = renderer.render(article, byline = byline, colors = colors)
            webViewState.loadHtml(rendered)
        }
    }

    ArticleStyleListener(webView = webViewState.webView)

    RestoreScrollState(scrollState = scrollState, lastScrollY = lastScrollY)

    DisposableEffect(articleID) {
        onDispose {
            webViewState.clearView()
        }
    }
}

@Composable
fun RestoreScrollState(scrollState: ScrollState, lastScrollY: MutableIntState) {
    LaunchedEffect(scrollState.maxValue) {
        if (scrollState.maxValue > 0 && lastScrollY.intValue > 0) {
            scrollState.scrollTo(lastScrollY.intValue)
            lastScrollY.intValue = 0
        }
    }
}
