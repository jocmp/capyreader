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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.ui.articles.ArticleRelations
import com.capyreader.app.ui.articles.LocalFullContent
import com.capyreader.app.ui.components.WebViewState
import com.capyreader.app.ui.components.pullrefresh.PullRefresh
import com.jocmp.capy.Article
import org.koin.compose.koinInject

@Composable
fun ArticleView(
    article: Article,
    articles: List<Article?>,
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    enableBackHandler: Boolean = false,
    onRequestArticle: (index: Int, id: String) -> Unit
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
                    SwipeRefresh(
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
fun SwipeRefresh(
    article: Article,
    onRequestArticle: (index: Int, id: String) -> Unit,
    articles: List<Article?>,
    content: @Composable () -> Unit,
) {
    val articlePosition =
        remember(article.id, articles.size) { ArticleRelations.from(article, articles) }

    val selectArticle = { index: Int ->
        articles.getOrNull(index)?.let { related ->
            onRequestArticle(index, related.id)
        }
    }

    PullRefresh(
        swipeEnabled = articlePosition.hasPrevious(),
        onRefresh = { selectArticle(articlePosition.previous) },
        indicatorPadding = PaddingValues(top = TopBarContainerHeight)
    ) {
        PullRefresh(
            swipeEnabled = articlePosition.hasNext(),
            indicatorAlignment = Alignment.BottomCenter,
            onRefresh = { selectArticle(articlePosition.next) }
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
