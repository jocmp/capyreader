package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jocmp.capy.Account
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.articles.ListArticle
import com.jocmp.capy.articles.ListFeed
import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.articles.StatusChange
import com.jocmp.capy.persistence.MAX_PAGE_IDS
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration.Companion.milliseconds

@Stable
internal class SessionArticles(
    private val account: Account,
    private val listSnapshot: ArticleListSnapshot,
    private val filter: ArticleFilter,
    private val sortOrder: SortOrder,
    private val since: OffsetDateTime,
    private val isCurrent: (SessionArticles) -> Boolean,
) : ArticleItems {
    private val syncMutex = Mutex()

    private val sharedStrings = HashMap<String, String>()

    private val localChanges = ConcurrentLinkedQueue<LocalChange>()

    private var rows by mutableStateOf(Rows.EMPTY)

    private var feeds by mutableStateOf(emptyMap<String, ListFeed>())

    override var isLoaded by mutableStateOf(false)
        private set

    override val itemCount: Int
        get() = rows.entries.size

    @Composable
    override fun rowAt(index: Int): Article? {
        val entry = entryAt(index) ?: return null
        val article = entry.article ?: return null
        val feed = article.feedID?.let(feeds::get)
        val read = entry.read
        val starred = entry.starred

        return remember(article, feed, read, starred) { article.toArticle(read, starred, feed) }
    }

    override fun peekID(index: Int): String? = entryAt(index)?.id

    override fun peekPublishedAt(index: Int): ZonedDateTime? {
        return entryAt(index)?.article?.publishedAtDateTime
    }

    override fun idAt(index: Int): String? = entryAt(index)?.takeIf { it.article != null }?.id

    fun entryAt(index: Int): ArticleListEntry? = rows.entries.getOrNull(index)

    override fun key(index: Int): Any = entryAt(index)?.id ?: index

    override fun indexOf(articleID: String): Int = rows.indexOf(articleID)

    fun start(scope: CoroutineScope, dispatcher: CoroutineDispatcher) {
        scope.launch(dispatcher) {
            sync()
        }

        scope.launch(dispatcher) {
            account.statusChanges.collect(::applyLocal)
        }

        scope.launch(dispatcher) {
            val joins = account.statusChanges.filter(::canJoinList)

            merge(account.listChanges, account.refreshes, joins)
                .debounce(CHANGE_DEBOUNCE)
                .collect { sync() }
        }
    }

    suspend fun reload() = sync()

    fun close() {
        listSnapshot.clear(owner = this)
    }

    private suspend fun sync() = syncMutex.withLock {
        val startedAt = System.nanoTime()
        feeds = account.listFeeds()
        val membership = account.sessionMembership(filter = filter, sortOrder = sortOrder, since = since)
        val current = rows
        val pending = ArrayList<ArticleListEntry>()

        val entries = membership.map { member ->
            val entry = current.entryFor(member.id)
                ?.also { it.applySynced(member.read, member.starred, startedAt) }
                ?: ArticleListEntry(member.id, member.read, member.starred)

            if (entry.article == null) {
                pending.add(entry)
            }
            entry
        }

        val next = Rows(entries)
        publish(next)
        replayLocalChanges(next, since = startedAt)

        // In list order, so the first screen fills before the rest of the list.
        pending.chunked(MAX_PAGE_IDS).forEach { batch ->
            val loaded = account.listArticlesByIDs(batch.map { it.id }, sharedStrings)
                .associateBy { it.id }

            batch.forEach { entry -> entry.article = loaded[entry.id] }
            isLoaded = true
        }
        isLoaded = true

        if (pending.any { it.article == null }) {
            publish(Rows(entries.filter { it.article != null }))
        }
    }

    private fun publish(next: Rows) {
        rows = next

        if (isCurrent(this)) {
            listSnapshot.set(owner = this, filter, sortOrder, next.ids, next::indexOf)
        }
    }

    private fun applyLocal(change: StatusChange) {
        val changedAt = System.nanoTime()
        localChanges.add(LocalChange(change, changedAt))

        val current = rows
        change.articleIDs.forEach { id ->
            current.entryFor(id)?.applyLocal(change, changedAt)
        }
    }

    // Membership was read before these landed, so rows the sync just added need them.
    private fun replayLocalChanges(rows: Rows, since: Long) {
        localChanges.removeIf { it.changedAt < since }

        localChanges.forEach { (change, changedAt) ->
            change.articleIDs.forEach { id -> rows.entryFor(id)?.applyLocal(change, changedAt) }
        }
    }

    private fun canJoinList(change: StatusChange): Boolean {
        return when (filter.status) {
            ArticleStatus.STARRED -> change.starred == true
            ArticleStatus.UNREAD -> change.read == false
            ArticleStatus.ALL -> false
        }
    }

    private data class LocalChange(val change: StatusChange, val changedAt: Long)

    private class Rows(val entries: List<ArticleListEntry>) {
        private val indexes: Map<String, Int> =
            HashMap<String, Int>(entries.size * 2).also { map ->
                entries.forEachIndexed { index, entry -> map[entry.id] = index }
            }

        val ids: List<String> = object : AbstractList<String>() {
            override val size get() = entries.size
            override fun get(index: Int) = entries[index].id
        }

        fun indexOf(articleID: String): Int = indexes[articleID] ?: -1

        fun entryFor(articleID: String): ArticleListEntry? = indexes[articleID]?.let(entries::get)

        companion object {
            val EMPTY = Rows(emptyList())
        }
    }

    companion object {
        private val CHANGE_DEBOUNCE = 500.milliseconds
    }
}

@Stable
internal class ArticleListEntry(
    val id: String,
    read: Boolean,
    starred: Boolean,
) {
    var article by mutableStateOf<ListArticle?>(null)

    private var status by mutableIntStateOf(statusBits(read, starred))

    private var changedLocallyAt = 0L

    val read: Boolean
        get() = status and READ != 0

    val starred: Boolean
        get() = status and STARRED != 0

    @Synchronized
    fun applyLocal(change: StatusChange, changedAt: Long) {
        if (changedAt < changedLocallyAt) return

        status = statusBits(change.read ?: read, change.starred ?: starred)
        changedLocallyAt = changedAt
    }

    @Synchronized
    fun applySynced(read: Boolean, starred: Boolean, syncStartedAt: Long) {
        if (changedLocallyAt < syncStartedAt) {
            status = statusBits(read, starred)
        }
    }

    private companion object {
        const val READ = 1
        const val STARRED = 2

        fun statusBits(read: Boolean, starred: Boolean) =
            (if (read) READ else 0) or (if (starred) STARRED else 0)
    }
}
