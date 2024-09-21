package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.jocmp.capy.Article
import kotlinx.coroutines.flow.Flow

data class IndexedArticles(
    private val index: Int,
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
        return next < articles.size
    }
}

@Composable
fun rememberIndexedArticles(
    article: Article,
    articles: Flow<PagingData<Article>>
): IndexedArticles {
    val snapshot = articles.collectAsLazyPagingItems().itemSnapshotList

    return remember(article, snapshot.size) {
        val index = snapshot.indexOfFirst { it?.id == article.id }

        IndexedArticles(
            index = index,
            previous = index - 1,
            next = index + 1,
            articles = snapshot
        )
    }
}