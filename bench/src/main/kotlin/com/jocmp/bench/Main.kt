// Created by Josiah Campbell
package com.jocmp.bench

import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val benchDir = File("bench").let {
        if (it.exists()) it else File(".")
    }

    val command = args.firstOrNull() ?: run {
        printUsage()
        return
    }

    if (command == "reset") {
        commandReset(benchDir)
        return
    }

    val config = try {
        loadConfig(benchDir)
    } catch (e: IllegalArgumentException) {
        System.err.println(e.message.orEmpty())
        exitProcess(1)
    }

    val (_, account) = try {
        loadOrCreateAccount(benchDir, config)
    } catch (e: Throwable) {
        System.err.println("Login failed: ${e.message.orEmpty()}")
        exitProcess(1)
    }

    runBlocking {
        when (command) {
            "login" -> commandLogin(account)
            "refresh" -> commandRefresh(account)
            "refresh-profile" -> commandRefreshProfile(account)
            "select-profile" -> commandSelectProfile(account)
            "add-feed" -> {
                val url = args.getOrNull(1) ?: run {
                    System.err.println("Usage: add-feed <url>")
                    exitProcess(1)
                }
                commandAddFeed(account, url)
            }
            "feeds" -> commandFeeds(account)
            "folders" -> commandFolders(account)
            "articles" -> commandArticles(account, status = args.getOrNull(1))
            "mark-read" -> {
                val id = args.getOrNull(1) ?: run {
                    System.err.println("Usage: mark-read <article-id>")
                    exitProcess(1)
                }
                commandMarkRead(account, id)
            }
            "mark-starred" -> {
                val id = args.getOrNull(1) ?: run {
                    System.err.println("Usage: mark-starred <article-id>")
                    exitProcess(1)
                }
                commandMarkStarred(account, id)
            }
            else -> {
                System.err.println("Unknown command: $command")
                printUsage()
            }
        }
    }

    exitProcess(0)
}

private fun printUsage() {
    println(
        """
        Usage: bench <command> [args]

        Commands:
          login                       Verify credentials and create local account
          refresh                     Sync all feeds
          refresh-profile             Sync with network vs query breakdown
          add-feed <url>              Add a feed by URL
          feeds                       List all feeds
          folders                     List folders/categories with feed counts
          articles [status]           Count articles. status: all|unread|starred (default: summary of all three)
          mark-read <article-id>      Toggle read state for an article
          mark-starred <article-id>   Toggle starred state for an article
          reset                       Delete bench/data/ and start fresh
        """.trimIndent()
    )
}
