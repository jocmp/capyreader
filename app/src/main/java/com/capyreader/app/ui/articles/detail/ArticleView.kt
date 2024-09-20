package com.capyreader.app.ui.articles.detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.ui.articles.ArticleRelations
import com.capyreader.app.ui.articles.LocalFullContent
import com.capyreader.app.ui.components.WebViewState
import com.jocmp.capy.Article
import kotlinx.coroutines.flow.Flow
import org.koin.compose.koinInject

@Composable
fun ArticleView(
    article: Article,
    articles: Flow<PagingData<Article>>,
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    enableBackHandler: Boolean = false,
    selectArticle: (index: Int, id: String) -> Unit
) {
    val snapshotList = articles.collectAsLazyPagingItems().itemSnapshotList
    val relations = remember(article, snapshotList) { ArticleRelations.from(article, snapshotList) }
    val showBars = true // canShowTopBar(webViewState)
    val fullContent = LocalFullContent.current
    val articleIndex by remember(article.id) { derivedStateOf { snapshotList.indexOfFirst { it?.id == article.id } } }
    val pagerState = rememberPagerState(
        initialPage = articleIndex.coerceAtLeast(0)
    ) {
        snapshotList.size
    }
//
//    val selectArticle = { index: Int ->
//        snapshotList.getOrNull(index)?.let { related ->
//            selectArticle(index, related.id)
//        }
//    }
//
//    val selectNextArticle = {
//        snapshotList.getOrNull(relations.next)?.let { related ->
//            selectArticle(relations.next, related.id)
//        }
//    }

    fun onToggleExtractContent() {
        if (article.fullContent == Article.FullContentState.LOADED) {
            fullContent.reset()
        } else if (article.fullContent != Article.FullContentState.LOADING) {
            fullContent.fetch()
        }
    }

    Scaffold { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
        ) {
            HorizontalPager(state = pagerState) { page ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    val pageArticle = snapshotList.getOrNull(page)

                    val current = if (pageArticle?.id == article.id) {
                        article
                    } else {
                        pageArticle
                    }

                    if (current != null) {
                        ArticleReader(
                            article = current
                        )
                    } else {
                        CapyPlaceholder()
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

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        val currentArticle = snapshotList.getOrNull(pagerState.currentPage)

        if (pagerState.isScrollInProgress) {
            return@LaunchedEffect
        }

        if (currentArticle != null && currentArticle.id != article.id) {
            selectArticle(pagerState.currentPage, currentArticle.id)
        }
    }

    LaunchedEffect(article.id) {
        if (articleIndex > -1 && pagerState.currentPage != articleIndex) {
            pagerState.scrollToPage(articleIndex)
        }
    }

    BackHandler(enableBackHandler) {
        onBackPressed()
    }
}

@Composable
fun canShowTopBar(
    webViewState: WebViewState,
    appPreferences: AppPreferences = koinInject(),
): Boolean {
    val pinTopBar by appPreferences.pinArticleTopBar
        .stateIn(rememberCoroutineScope())
        .collectAsState()

    return pinTopBar
}
