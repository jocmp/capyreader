package com.capyreader.app.ui.articles.detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.ui.articles.IndexedArticles
import com.capyreader.app.ui.articles.LocalFullContent
import com.capyreader.app.ui.components.pullrefresh.SwipeRefresh
import com.jocmp.capy.Article
import org.koin.compose.koinInject

@Composable
fun ArticleView(
    article: Article,
    articles: IndexedArticles,
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    enableBackHandler: Boolean = false,
    onRequestArticle: (id: String) -> Unit
) {
    val fullContent = LocalFullContent.current
    val scrollState = rememberSaveable(article.id, key = article.id, saver = ScrollState.Saver) {
        ScrollState(initial = 0)
    }

    fun selectArticle(relation: () -> Article?) {
        relation()?.let { onRequestArticle(it.id) }
    }

    val onRequestPrevious = {
        selectArticle { articles.previous() }
    }

    val onRequestNext = {
        selectArticle { articles.next() }
    }

    fun onToggleExtractContent() {
        if (article.fullContent == Article.FullContentState.LOADED) {
            fullContent.reset()
        } else if (article.fullContent != Article.FullContentState.LOADING) {
            fullContent.fetch()
        }
    }

    val showBars = canShowBars(scrollState)

    Scaffold { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                Column {
                    ArticlePullRefresh(
                        article,
                        showBars,
                        articles = articles,
                        onRequestPrevious = onRequestPrevious,
                        onRequestNext = onRequestNext,
                    ) {
                        ArticleReader(
                            article = article,
                            scrollState = scrollState
                        )
                    }
                }

                BarVisibility(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    visible = showBars
                ) {
                    ArticleBottomBar(
                        onRequestNext = onRequestNext
                    )
                }
            }

            BarVisibility(visible = showBars) {
                ArticleTopBar(
                    article = article,
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
}

@Composable
fun ArticlePullRefresh(
    article: Article,
    showBars: Boolean,
    onRequestNext: () -> Unit,
    onRequestPrevious: () -> Unit,
    articles: IndexedArticles,
    content: @Composable () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    val triggerThreshold = {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    SwipeRefresh(
        onRefresh = { onRequestPrevious() },
        swipeEnabled = articles.hasPrevious(),
        indicatorPadding = PaddingValues(top = TopBarContainerHeight),
        onTriggerThreshold = { triggerThreshold() }
    ) {
        SwipeRefresh(
            onRefresh = { onRequestNext() },
            swipeEnabled = articles.hasNext(),
            onTriggerThreshold = { triggerThreshold() },
            indicatorPadding = PaddingValues(
                bottom = if (showBars) {
                    BottomBarTokens.ContainerHeight
                } else {
                    0.dp
                }
            ),
            indicatorAlignment = Alignment.BottomCenter,
        ) {
            key(article.id) {
                content()
            }
        }
    }
}

private val TopBarContainerHeight = 64.dp

@Composable
fun BoxScope.BarVisibility(
    modifier: Modifier = Modifier,
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun canShowBars(
    scrollState: ScrollState,
    appPreferences: AppPreferences = koinInject(),
): Boolean {
    val pinBars by appPreferences.pinArticleTopBar
        .stateIn(rememberCoroutineScope())
        .collectAsState()

    return pinBars ||
            scrollState.lastScrolledBackward ||
            scrollState.value == 0
}
