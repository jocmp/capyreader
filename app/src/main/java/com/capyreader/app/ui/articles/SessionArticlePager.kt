package com.capyreader.app.ui.articles

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.jocmp.capy.Account
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.articles.ArticleStatusOverlay
import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.articles.StatusChange
import com.jocmp.capy.articles.StatusOverride
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.OffsetDateTime
import kotlin.time.Duration.Companion.milliseconds

/**
 * The article list for one list session.
 *
 * Membership is a snapshot of article IDs taken with the session's read cutoff, so reading an
 * article never changes the list. Rows pick up local read and starred writes through [overrides].
 * The snapshot reloads with the same cutoff for new or deleted articles, feed edits, refreshes
 * (which can change statuses from the server), and mark all read.
 */
internal class SessionArticlePager(
    private val account: Account,
    private val listSnapshot: ArticleListSnapshot,
    private val filter: ArticleFilter,
    private val sortOrder: SortOrder,
    private val since: OffsetDateTime,
    private val isCurrent: (SessionArticlePager) -> Boolean,
) {
    private val overlay = ArticleStatusOverlay()

    private val reloadMutex = Mutex()

    private val idsLock = Any()

    private var articleIDs: List<String> = emptyList()

    @Volatile
    private var source: SnapshotPagingSource? = null

    val overrides: StateFlow<Map<String, StatusOverride>> = overlay.overrides

    val articles: Flow<PagingData<Article>> = flow {
        reloadMutex.withLock { loadSnapshot() }

        val pager = Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = 10,
                initialLoadSize = PAGE_SIZE * 3,
            ),
            pagingSourceFactory = ::createSource,
        )

        emitAll(pager.flow)
    }

    /** Keeps the session in step with the account while [scope] is active. */
    fun bind(scope: CoroutineScope, dispatcher: CoroutineDispatcher) {
        scope.launch(dispatcher) {
            account.statusChanges.collect(overlay::apply)
        }

        scope.launch(dispatcher) {
            val joins = account.statusChanges.filter(::canJoinList)

            merge(account.listChanges, account.refreshes, joins)
                .debounce(CHANGE_DEBOUNCE)
                .collect { reload() }
        }
    }

    /** Takes a fresh snapshot with the session's cutoff. */
    suspend fun reload() {
        // Serialized so a slower, older query can't publish over a newer one.
        reloadMutex.withLock { loadSnapshot() }
        source?.invalidate()
    }

    /**
     * Whether [change] can add an article this list doesn't have yet, like starring an article
     * from search while the Starred list is open.
     */
    private fun canJoinList(change: StatusChange): Boolean {
        return when (filter.status) {
            ArticleStatus.STARRED -> change.starred == true
            ArticleStatus.UNREAD -> change.read == false
            ArticleStatus.ALL -> false
        }
    }

    private suspend fun loadSnapshot() {
        val ids = account.sessionArticleIDs(filter = filter, sortOrder = sortOrder, since = since)
        publish(ids)
    }

    private fun createSource(): SnapshotPagingSource {
        val ids = synchronized(idsLock) { articleIDs }

        return SnapshotPagingSource(
            articleIDs = ids,
            loadArticles = account::articlesByIDs,
            onMissing = ::prune,
            // Fresh rows as of startedAt, so older overrides for them are redundant.
            onPageLoaded = { loadedIDs, startedAt -> overlay.clearBefore(startedAt, loadedIDs) },
        ).also { source = it }
    }

    private suspend fun prune(missing: Set<String>) {
        reloadMutex.withLock {
            val ids = synchronized(idsLock) { articleIDs.filterNot { it in missing } }
            publish(ids)
        }
    }

    private fun publish(ids: List<String>) {
        synchronized(idsLock) { articleIDs = ids }

        if (isCurrent(this)) {
            listSnapshot.set(owner = this, filter, sortOrder, ids)
        }
    }

    /** Stops the reader from following this session once it's gone. */
    fun close() {
        listSnapshot.clear(owner = this)
    }

    companion object {
        const val PAGE_SIZE = 100

        /** Coalesces a sync's many article writes into one snapshot reload. */
        private val CHANGE_DEBOUNCE = 500.milliseconds
    }
}
