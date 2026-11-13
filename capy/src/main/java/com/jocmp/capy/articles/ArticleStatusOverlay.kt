package com.jocmp.capy.articles

import com.jocmp.capy.Article
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Read and starred changes for rows a list has already loaded, keyed by article ID.
 *
 * Lists render rows through the overlay instead of re-querying after every status write, the way
 * NetNewsWire shares one status object per article. Each list session owns one, fed by
 * [com.jocmp.capy.Account.statusChanges].
 */
class ArticleStatusOverlay {
    private val _overrides = MutableStateFlow<Map<String, StatusOverride>>(emptyMap())

    val overrides: StateFlow<Map<String, StatusOverride>> = _overrides.asStateFlow()

    fun apply(change: StatusChange) {
        if (change.articleIDs.isEmpty()) return

        val changedAt = now()

        _overrides.update { current ->
            current + change.articleIDs.associateWith { id ->
                val existing = current[id] ?: StatusOverride()

                existing.copy(
                    read = change.read ?: existing.read,
                    starred = change.starred ?: existing.starred,
                    changedAt = changedAt,
                )
            }
        }
    }

    /**
     * Drops overrides for [articleIDs] from before [time], a value from [now]. Rows loaded from the
     * database after [time] already include those changes, and may include newer ones from sync.
     */
    fun clearBefore(time: Long, articleIDs: Collection<String>) {
        _overrides.update { current ->
            val stale = articleIDs.filter { id -> current[id]?.let { it.changedAt < time } == true }

            if (stale.isEmpty()) current else current - stale.toSet()
        }
    }

    /** Drops overrides for articles outside [articleIDs], such as ones that left the list. */
    fun retainOnly(articleIDs: Set<String>) {
        _overrides.update { current ->
            if (current.keys.all(articleIDs::contains)) current else current.filterKeys(articleIDs::contains)
        }
    }

    companion object {
        /** A monotonic timestamp comparable with each override's [StatusOverride.changedAt]. */
        fun now(): Long = System.nanoTime()
    }
}

/** A local read or starred write. Null fields are unchanged. */
data class StatusChange(
    val articleIDs: List<String>,
    val read: Boolean? = null,
    val starred: Boolean? = null,
)

data class StatusOverride(
    val read: Boolean? = null,
    val starred: Boolean? = null,
    val changedAt: Long = 0,
)

fun Article.withStatus(override: StatusOverride?): Article {
    override ?: return this

    val read = override.read ?: read
    val starred = override.starred ?: starred

    if (read == this.read && starred == this.starred) {
        return this
    }

    return copy(read = read, starred = starred)
}
