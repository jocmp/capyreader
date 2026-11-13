package com.capyreader.app.ui.articles

import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.articles.SortOrder

class ArticleListSnapshot {
    @Volatile
    private var current: Snapshot? = null

    @Synchronized
    fun set(
        owner: Any,
        filter: ArticleFilter,
        sortOrder: SortOrder,
        articleIDs: List<String>,
        indexOf: (String) -> Int,
    ) {
        current = Snapshot(owner, filter, sortOrder, articleIDs, indexOf)
    }

    @Synchronized
    fun clear(owner: Any) {
        if (current?.owner === owner) {
            current = null
        }
    }

    fun neighbors(
        filter: ArticleFilter,
        sortOrder: SortOrder,
        articleID: String,
    ): Pair<String?, String?>? {
        val snapshot = current ?: return null

        if (snapshot.filter != filter || snapshot.sortOrder != sortOrder) {
            return null
        }

        val index = snapshot.indexOf(articleID).takeIf { it >= 0 } ?: return null

        return snapshot.articleIDs.getOrNull(index - 1) to snapshot.articleIDs.getOrNull(index + 1)
    }

    private class Snapshot(
        val owner: Any,
        val filter: ArticleFilter,
        val sortOrder: SortOrder,
        val articleIDs: List<String>,
        val indexOf: (String) -> Int,
    )
}
