package com.jocmp.bench

import com.jocmp.capy.Account
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.persistence.ArticleRecords
import kotlinx.coroutines.flow.first
import java.io.File
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

suspend fun commandRefresh(account: Account) {
    val (result, totalDuration) = measureTimedValue {
        account.refresh()
    }
    result.getOrThrow()

    val feedCount = account.allFeeds.first().size
    val unread = account.countAllByStatus(ArticleStatus.UNREAD).first()
    val total = account.countAllByStatus(ArticleStatus.ALL).first()

    println("refresh completed in ${totalDuration.inWholeMilliseconds}ms")
    println("  feeds: $feedCount")
    println("  articles: $total total, $unread unread")
}

suspend fun commandRefreshProfile(account: Account) {
    val (_, preCountTime) = measureTimedValue {
        account.countAllByStatus(ArticleStatus.ALL).first()
    }

    val (_, refreshTime) = measureTimedValue {
        account.refresh().getOrThrow()
    }

    val (feedCount, feedQueryTime) = measureTimedValue {
        account.allFeeds.first().size
    }

    val (articleTotal, countQueryTime) = measureTimedValue {
        account.countAllByStatus(ArticleStatus.ALL).first()
    }

    val unread = account.countAllByStatus(ArticleStatus.UNREAD).first()

    println("=== refresh profile ===")
    println()
    println("  sync (network + db writes): ${refreshTime.inWholeMilliseconds}ms")
    println("  pre-refresh count query:    ${preCountTime.inWholeMilliseconds}ms")
    println("  post-refresh feed query:    ${feedQueryTime.inWholeMilliseconds}ms")
    println("  post-refresh count query:   ${countQueryTime.inWholeMilliseconds}ms")
    println()
    println("  feeds: $feedCount")
    println("  articles: $articleTotal total, $unread unread")
}

suspend fun commandAddFeed(account: Account, url: String) {
    val duration = measureTime {
        val result = account.addFeed(url = url)
        println("  result: $result")
    }

    println("add-feed completed in ${duration.inWholeMilliseconds}ms")
}

suspend fun commandFeeds(account: Account) {
    val feeds = account.allFeeds.first()
    println("${feeds.size} feeds:")
    feeds.forEach { feed ->
        println("  [${feed.id}] ${feed.title} (${feed.count} unread)")
    }
}

suspend fun commandArticles(account: Account) {
    val all = account.countAllByStatus(ArticleStatus.ALL).first()
    val unread = account.countAllByStatus(ArticleStatus.UNREAD).first()
    val starred = account.countAllByStatus(ArticleStatus.STARRED).first()

    println("articles:")
    println("  all:     $all")
    println("  unread:  $unread")
    println("  starred: $starred")
}

suspend fun commandSelectProfile(account: Account) {
    val records = ArticleRecords(account.database)
    val pageSize = 50L

    val total = account.countAllByStatus(ArticleStatus.ALL).first()

    val (pageCount, pageDuration) = measureTimedValue {
        var offset = 0L
        var pages = 0

        while (offset < total) {
            records.byStatus.all(
                status = ArticleStatus.ALL,
                limit = pageSize,
                offset = offset,
                sortOrder = SortOrder.NEWEST_FIRST,
            ).executeAsList()
            offset += pageSize
            pages++
        }

        pages
    }

    val firstArticleID = records.byStatus.all(
        status = ArticleStatus.ALL,
        limit = 1,
        offset = 0,
        sortOrder = SortOrder.NEWEST_FIRST,
    ).executeAsOneOrNull()?.id

    val (_, findDuration) = measureTimedValue {
        if (firstArticleID != null) {
            records.find(firstArticleID)
        }
    }

    val (_, countAllDuration) = measureTimedValue {
        records.countAll(ArticleStatus.UNREAD).first()
    }

    val (_, countSearchDuration) = measureTimedValue {
        records.countAllBySavedSearch(ArticleStatus.UNREAD).first()
    }

    val (_, unreadCountDuration) = measureTimedValue {
        records.countUnread(
            filter = ArticleFilter.default(),
            query = null,
        ).first()
    }

    val boundariesProvider = records.byStatus.pageBoundaries(
        status = ArticleStatus.ALL,
    )
    val queryProvider = records.byStatus.keyed(
        status = ArticleStatus.ALL,
        sortOrder = SortOrder.NEWEST_FIRST,
    )

    val (keyedPageCount, keyedDuration) = measureTimedValue {
        val boundaries = boundariesProvider(null, pageSize).executeAsList()
        var pages = 0

        for (i in boundaries.indices) {
            val begin = boundaries[i]
            val end = boundaries.getOrNull(i + 1)
            queryProvider(begin, end).executeAsList()
            pages++
        }

        pages
    }

    // Collect all IDs from both approaches to verify correctness
    val offsetIDs = mutableListOf<String>()
    run {
        var offset = 0L
        while (offset < total) {
            records.byStatus.all(
                status = ArticleStatus.ALL,
                limit = pageSize,
                offset = offset,
                sortOrder = SortOrder.NEWEST_FIRST,
            ).executeAsList().forEach { offsetIDs.add(it.id) }
            offset += pageSize
        }
    }

    val keyedIDs = mutableListOf<String>()
    run {
        val boundaries = boundariesProvider(null, pageSize).executeAsList()
        for (i in boundaries.indices) {
            val begin = boundaries[i]
            val end = boundaries.getOrNull(i + 1)
            queryProvider(begin, end).executeAsList().forEach { keyedIDs.add(it.id) }
        }
    }

    val orderMatch = offsetIDs == keyedIDs
    val sizeMatch = offsetIDs.size == keyedIDs.size

    println("=== select profile ===")
    println()
    println("  page through all OFFSET ($pageCount pages of $pageSize): ${pageDuration.inWholeMilliseconds}ms")
    println("  page through all KEYED  ($keyedPageCount pages of $pageSize): ${keyedDuration.inWholeMilliseconds}ms")
    println("  correctness: ${if (orderMatch) "MATCH" else "MISMATCH"} (offset=${offsetIDs.size}, keyed=${keyedIDs.size})")
    if (!orderMatch && sizeMatch) {
        val firstDiff = offsetIDs.zip(keyedIDs).indexOfFirst { (a, b) -> a != b }
        println("  first difference at index $firstDiff: offset=${offsetIDs[firstDiff]}, keyed=${keyedIDs[firstDiff]}")
    }
    println("  find single article:                    ${findDuration.inWholeMilliseconds}ms")
    println("  count all by feed (unread):              ${countAllDuration.inWholeMilliseconds}ms")
    println("  count all by saved search (unread):      ${countSearchDuration.inWholeMilliseconds}ms")
    println("  count unread (status filter):            ${unreadCountDuration.inWholeMilliseconds}ms")
    println()
    println("  articles: $total")
}

fun commandReset(benchDir: File) {
    val dataDir = File(benchDir, "data")
    if (dataDir.exists()) {
        dataDir.deleteRecursively()
        println("reset: deleted bench/data/")
    } else {
        println("reset: nothing to delete")
    }
}
