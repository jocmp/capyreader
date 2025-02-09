package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.paging.compose.LazyPagingItems
import com.jocmp.capy.Article

data class IndexedArticles(
    val canScroll: Boolean,
    val index: Int,
    private val next: Int,
    private val previous: Int,
    private val articles: List<Article?>,
) {
    fun next() = articles.getOrNull(next)

    fun previous() = articles.getOrNull(previous)

    fun hasPrevious(): Boolean {
        return previous > -1
    }

    fun hasNext(): Boolean {
        return next < size
    }

    fun find(page: Int) = articles.getOrNull(page)

    val size = articles.size
}

@Composable
fun rememberIndexedArticles(
    article: Article, articles: LazyPagingItems<Article>): IndexedArticles {
    val lookup = LocalArticleLookup.current
    val snapshot = articles.itemSnapshotList

    var indexedArticles by remember { mutableStateOf(nullIndexedArticles()) }

    LaunchedEffect(article.id, snapshot.size) {
        val index = lookup.findIndex(article.id)
        val isValidIndex = index >= 0

        indexedArticles = IndexedArticles(
            canScroll = isValidIndex,
            index = if (isValidIndex) index else -1,
            previous = index - 1,
            next = index + 1,
            articles = snapshot
        )
    }

    return indexedArticles
}

fun nullIndexedArticles() = IndexedArticles(
    canScroll = false,
    index = 0,
    previous = 0,
    next = 0,
    articles = emptyList()
)
