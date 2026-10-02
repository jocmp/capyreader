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

class PresentedArticles(
    val filter: ArticleFilter,
    val sortOrder: SortOrder,
    val items: LazyPagingItems<Article>,
)

@Composable
fun rememberPresentedArticles(page: ArticleListPage): PresentedArticles {
    val items = key(page) { page.articles.collectAsLazyPagingItems() }
    var presented by remember { mutableStateOf(PresentedArticles(page.filter, page.sortOrder, items)) }

    LaunchedEffect(items) {
        snapshotFlow { items.loadState.refresh }.first { it !is LoadState.Loading }
        presented = PresentedArticles(page.filter, page.sortOrder, items)
    }

    return presented
}
