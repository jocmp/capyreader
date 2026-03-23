package com.jocmp.bench

import com.jocmp.capy.Account
import com.jocmp.capy.ArticleStatus
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

fun commandReset(benchDir: File) {
    val dataDir = File(benchDir, "data")
    if (dataDir.exists()) {
        dataDir.deleteRecursively()
        println("reset: deleted bench/data/")
    } else {
        println("reset: nothing to delete")
    }
}
