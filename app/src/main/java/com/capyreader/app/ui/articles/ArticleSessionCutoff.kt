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

    /** Written by the list when its session snapshot starts, so the reader's neighbors match it exactly. */
    fun set(value: OffsetDateTime) {
        this.value = value
    }

    /**
     * Starts a session cutoff if one isn't already set — the fallback for cold deep links (no list
     * session). Idempotent: it won't overwrite the list's cutoff, and repeated reader opens
     * (next/previous) keep the same session start.
     */
    fun start() {
        if (value == null) {
            value = OffsetDateTime.now()
        }
    }
}
