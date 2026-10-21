package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.articles.SortOrder
import kotlinx.coroutines.flow.first
import java.time.OffsetDateTime

class PresentedArticles(
    val filter: ArticleFilter,
    val sortOrder: SortOrder,
    val since: OffsetDateTime,
    val refreshed: Boolean,
    val items: LazyPagingItems<Article>,
)

@Composable
fun rememberPresentedArticles(page: ArticleListPage): PresentedArticles {
    val items = key(page) { page.articles.collectAsLazyPagingItems() }
    var presented by remember { mutableStateOf(presentedArticles(page, items)) }

    LaunchedEffect(items) {
        snapshotFlow { items.loadState.refresh }.first { it !is LoadState.Loading }
        presented = presentedArticles(page, items)
    }

    return presented
}

private fun presentedArticles(page: ArticleListPage, items: LazyPagingItems<Article>) =
    PresentedArticles(
        filter = page.filter,
        sortOrder = page.sortOrder,
        since = page.since,
        refreshed = page.refreshed,
        items = items,
    )
