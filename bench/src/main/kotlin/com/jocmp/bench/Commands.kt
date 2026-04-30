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

suspend fun commandArticles(account: Account, status: String? = null) {
    val filter = when (status?.lowercase()) {
        null -> null
        "all" -> ArticleStatus.ALL
        "unread" -> ArticleStatus.UNREAD
        "starred" -> ArticleStatus.STARRED
        else -> {
            System.err.println("Unknown status: $status. Expected: all|unread|starred")
            return
        }
    }

    if (filter == null) {
        val all = account.countAllByStatus(ArticleStatus.ALL).first()
        val unread = account.countAllByStatus(ArticleStatus.UNREAD).first()
        val starred = account.countAllByStatus(ArticleStatus.STARRED).first()

        println("articles:")
        println("  all:     $all")
        println("  unread:  $unread")
        println("  starred: $starred")
        return
    }

    val records = ArticleRecords(account.database)
    val articles = records.byStatus.all(
        status = filter,
        limit = 50,
        offset = 0,
        sortOrder = SortOrder.NEWEST_FIRST,
    ).executeAsList()

    val count = account.countAllByStatus(filter).first()
    println("articles ($filter): $count total — showing first ${articles.size}")
    articles.forEach { article ->
        val flags = buildString {
            if (article.read) append("r") else append("-")
            if (article.starred) append("s") else append("-")
        }
        println("  [${article.id}] [$flags] ${article.title}")
    }
}

suspend fun commandLogin(account: Account) {
    println("Logged in as ${account.preferences.username.get()}")
    println("  source: ${account.source.value}")
    println("  url:    ${account.preferences.url.get()}")
}

suspend fun commandFolders(account: Account) {
    val folders = account.folders.first()
    val ungrouped = account.feeds.first()
    println("${folders.size} folders, ${ungrouped.size} ungrouped feeds:")
    folders.forEach { folder ->
        println("  ${folder.title} (${folder.feeds.size} feeds)")
    }
    if (ungrouped.isNotEmpty()) {
        println("  (no folder): ${ungrouped.size} feeds")
    }
}

suspend fun commandMarkRead(account: Account, articleID: String) {
    val article = account.findArticle(articleID) ?: run {
        System.err.println("Article $articleID not found locally. Try 'refresh' first.")
        return
    }

    if (article.read) {
        account.markUnread(articleID).getOrThrow()
        println("marked unread: $articleID")
    } else {
        account.markRead(articleID).getOrThrow()
        println("marked read: $articleID")
    }

    account.sendArticleStatus().getOrThrow()
    println("synced status to remote")
}

suspend fun commandMarkStarred(account: Account, articleID: String) {
    val article = account.findArticle(articleID) ?: run {
        System.err.println("Article $articleID not found locally. Try 'refresh' first.")
        return
    }

    if (article.starred) {
        account.removeStar(articleID).getOrThrow()
        println("unstarred: $articleID")
    } else {
        account.addStar(articleID).getOrThrow()
        println("starred: $articleID")
    }

    account.sendArticleStatus().getOrThrow()
    println("synced status to remote")
}

suspend fun commandSelectProfile(account: Account) {
    val records = ArticleRecords(account.database)
    val pageSize = 100L

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

    println("=== select profile ===")
    println()
    println("  page through all ($pageCount pages of $pageSize): ${pageDuration.inWholeMilliseconds}ms")
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
