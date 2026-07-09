package com.capyreader.app.common

/**
 * Bridges hardware volume keys (handled by the Activity) to page-scrolling the
 * currently visible article reader (composed deep in the Compose tree).
 *
 * EinkBro-style paging: volume-down pages forward, volume-up pages back. The
 * reader screen registers a scroller keyed by article id; the Activity calls
 * [canHandle]/[handle] from dispatchKeyEvent. When a page-scroll can't advance
 * (already at the top/bottom of the article), [onAtBoundary] rolls to the
 * adjacent article so the whole feed reads with one thumb.
 *
 * Everything is touched only on the main thread (Compose + key dispatch), so no
 * synchronization is needed.
 */
object VolumeKeyPager {
    /** The article whose scroller should respond; null when no reader is open. */
    var currentArticleId: String? = null

    /** True when the "Volume keys turn pages" setting is on. */
    var isEnabled: () -> Boolean = { false }

    /** True when audio is playing, so volume keys yield to volume control. */
    var isAudioActive: () -> Boolean = { false }

    /** Advance to the next (forward=true) / previous article at a scroll edge. */
    var onAtBoundary: ((forward: Boolean) -> Unit)? = null

    // Scrollers registered per article id. Returns true if it will page-scroll,
    // false if already at the boundary.
    private val scrollers = mutableMapOf<String, (forward: Boolean) -> Boolean>()

    fun registerScroller(articleId: String, scroller: (forward: Boolean) -> Boolean) {
        scrollers[articleId] = scroller
    }

    fun unregisterScroller(articleId: String) {
        scrollers.remove(articleId)
    }

    /**
     * Pages the article *list* when no reader is open. Independent gating from
     * the reader so it survives a reader open/close (which resets [isEnabled]).
     */
    private var listScroller: ((forward: Boolean) -> Boolean)? = null
    var isListEnabled: () -> Boolean = { false }
    var isListAudioActive: () -> Boolean = { false }

    fun registerListScroller(scroller: (forward: Boolean) -> Boolean) {
        listScroller = scroller
    }

    fun unregisterListScroller() {
        listScroller = null
    }

    /** A reader is on screen when its scroller is registered as current. */
    private val readerActive: Boolean
        get() = scrollers[currentArticleId] != null

    /** Whether a volume-key press should be consumed for paging right now. */
    fun canHandle(): Boolean =
        if (readerActive) {
            isEnabled() && !isAudioActive()
        } else {
            listScroller != null && isListEnabled() && !isListAudioActive()
        }

    /** Page the current reader (rolling to the adjacent article at a boundary),
     *  or the article list when no reader is open. */
    fun handle(forward: Boolean) {
        if (readerActive) {
            val scroller = scrollers[currentArticleId] ?: return
            if (!scroller(forward)) {
                onAtBoundary?.invoke(forward)
            }
        } else {
            listScroller?.invoke(forward)
        }
    }
}
