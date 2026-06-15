package com.capyreader.app.ui.articles

import java.time.OffsetDateTime

/**
 * The "since" cutoff for the reader's next/previous neighbor query, owned by the reading session
 * rather than the list's refresh.
 *
 * Set when a reading session begins (the reader opens its first article, before it marks anything
 * read) and cleared when the session ends. This keeps articles read/unstarred during the session
 * pinned in the neighbor set, so swiping back to where you started works — independent of whether a
 * list was ever loaded, which is what makes it correct for cold deep links.
 */
class ArticleSessionCutoff {
    var value: OffsetDateTime? = null
        private set

    fun startIfNeeded() {
        if (value == null) {
            value = OffsetDateTime.now()
        }
    }

    fun clear() {
        value = null
    }
}
