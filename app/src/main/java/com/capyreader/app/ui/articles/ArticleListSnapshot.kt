package com.capyreader.app.ui.articles

import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.articles.SortOrder

/**
 * The ordered article IDs of the current list session, shared with the reader so next/previous
 * follows exactly what the list shows without a query per swipe.
 */
class ArticleListSnapshot {
    @Volatile
    private var current: Snapshot? = null

    /** [indexOf] finds an ID's position in [articleIDs], or -1. */
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

    /** Drops the snapshot if [owner] published it, so the reader falls back to querying. */
    @Synchronized
    fun clear(owner: Any) {
        if (current?.owner === owner) {
            current = null
        }
    }

    /**
     * Previous and next IDs around [articleID], or null when the snapshot doesn't cover it
     * (a different filter or sort, or an article outside the list, such as a deep link).
     */
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
