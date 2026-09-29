package com.capyreader.app.ui.articles

import java.time.OffsetDateTime

/**
 * The "since" cutoff for the reader's next/previous neighbor query, owned by the reading session
 * rather than the list's refresh.
 *
 * Set when a reading session begins (the reader opens its first article, before it marks anything
 * read) and [reset] when the session ends (the reader leaves the back stack). This keeps articles
 * read/unstarred during the session pinned in the neighbor set, so swiping back to where you
 * started works — independent of whether a list was ever loaded, which is what makes it correct for
 * cold deep links.
 */
class ArticleSessionCutoff {
    var value: OffsetDateTime? = null
        private set

    /** Written by the list when its session snapshot starts, so the reader's neighbors match it exactly. */
    fun set(value: OffsetDateTime) {
        this.value = value
    }

    /**
     * Begins a reading session. Sets the cutoff when none is active, and also pulls a *future*
     * cutoff back to now: the list stamps its snapshot slightly ahead ([ArticleScreenViewModel]
     * uses now + 1s), and the reader marks the opened article read at now — without this, that
     * article would fall before the cutoff and drop out of its own neighbor set (you couldn't swipe
     * back to it). Otherwise idempotent, so repeated reader opens (next/previous) keep the same
     * session start.
     */
    fun start() {
        val now = OffsetDateTime.now()
        if (value == null || value?.isAfter(now) == true) {
            value = now
        }
    }

    /** Ends the session so the next one starts fresh rather than reusing a stale cutoff. */
    fun reset() {
        value = null
    }
}
