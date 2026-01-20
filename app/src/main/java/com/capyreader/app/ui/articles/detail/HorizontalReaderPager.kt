package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import com.capyreader.app.ui.articles.LocalArticleNavigator
import com.jocmp.capy.Article
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter

@Composable
fun HorizontalReaderPager(
    article: Article,
    enabled: Boolean,
    content: @Composable (Article) -> Unit,
) {
    val navigator = LocalArticleNavigator.current

    if (!enabled || navigator == null) {
        return content(article)
    }

    val pages = listOfNotNull(
        navigator.previousArticle(),
        article,
        navigator.nextArticle()
    )

    val currentIndex = pages.indexOfFirst { it.id == article.id }.coerceAtLeast(0)

    val pagerState = rememberPagerState(
        initialPage = currentIndex,
        pageCount = { pages.size }
    )

    // Handle page changes - only when user has stopped scrolling
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage to pagerState.isScrollInProgress }
            .filter { (_, isScrolling) -> !isScrolling }
            .collectLatest { (settledPage, _) ->
                val targetArticle = pages.getOrNull(settledPage)
                if (targetArticle != null && targetArticle.id != article.id) {
                    navigator.selectArticle(targetArticle)
                }
            }
    }

    HorizontalPager(
        state = pagerState,
        key = { page -> pages.getOrNull(page)?.id ?: "empty-$page" }
    ) { page ->
        pages.getOrNull(page)?.let { pageArticle ->
            content(pageArticle)
        }
    }
}
