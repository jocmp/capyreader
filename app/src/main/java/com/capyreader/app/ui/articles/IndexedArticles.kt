package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.paging.compose.LazyPagingItems
import com.jocmp.capy.Article
import com.jocmp.capy.logging.CapyLog

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
    article: Article,
    articles: LazyPagingItems<Article>
): IndexedArticles {
    val snapshot = articles.itemSnapshotList

    return remember(article, snapshot.size) {
        val index = snapshot.indexOfFirst { it?.id == article.id }
        val isValidIndex = index > -1

        CapyLog.info(
            "remember", mapOf(
                "article_id" to article.id,
                "snapshot_size" to snapshot.size.toString(),
                "index" to index.toString(),
            )
        )

        IndexedArticles(
            canScroll = articles.loadState.isIdle && isValidIndex,
            index = if (isValidIndex) index else 0,
            previous = index - 1,
            next = index + 1,
            articles = snapshot
        )
    }
}
