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

    fun onToggleExtractContent() {
        if (article.fullContent == Article.FullContentState.LOADED) {
            fullContent.reset()
        } else if (article.fullContent != Article.FullContentState.LOADING) {
            fullContent.fetch()
        }
    }

    val showBars = canShowTopBar(scrollState)

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
                        articles = articles,
                        onRequestArticle = onRequestArticle
                    ) {
                        ArticleReader(
                            article = article,
                            scrollState = scrollState
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showBars,
            enter = fadeIn() + expandVertically(),
            exit = shrinkVertically() + fadeOut()
        ) {
            ArticleTopBar(
                article = article,
                onToggleExtractContent = ::onToggleExtractContent,
                onToggleRead = onToggleRead,
                onToggleStar = onToggleStar,
                onClose = onBackPressed
            )
        }
    }

    BackHandler(enableBackHandler) {
        onBackPressed()
    }
}

@Composable
fun ArticlePullRefresh(
    article: Article,
    onRequestArticle: (id: String) -> Unit,
    articles: IndexedArticles,
    content: @Composable () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    val triggerThreshold = {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    fun selectArticle(relation: () -> Article?) {
        relation()?.let { onRequestArticle(it.id) }
    }

    SwipeRefresh(
        onRefresh = { selectArticle { articles.previous() } },
        swipeEnabled = articles.hasPrevious(),
        indicatorPadding = PaddingValues(top = TopBarContainerHeight),
        onTriggerThreshold = { triggerThreshold() }
    ) {
        SwipeRefresh(
            onRefresh = { selectArticle { articles.next() } },
            swipeEnabled = articles.hasNext(),
            onTriggerThreshold = { triggerThreshold() },
            indicatorAlignment = Alignment.BottomCenter,
        ) {
            key(article.id) {
                content()
            }
        }
    }
}

val TopBarContainerHeight = 64.dp

@Composable
fun canShowTopBar(
    scrollState: ScrollState,
    appPreferences: AppPreferences = koinInject(),
): Boolean {
    val pinTopBar by appPreferences.pinArticleTopBar
        .stateIn(rememberCoroutineScope())
        .collectAsState()

    return pinTopBar ||
            scrollState.lastScrolledBackward ||
            scrollState.value == 0
}
