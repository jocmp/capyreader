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

    if (command == "login") {
        val config = loadConfig(benchDir)
        runBlocking { commandLogin(config) }
        exitProcess(0)
    }

    val config = loadConfig(benchDir)
    val (_, account) = loadOrCreateAccount(benchDir, config)

    runBlocking {
        when (command) {
            "refresh" -> commandRefresh(account)
            "refresh-profile" -> commandRefreshProfile(account)
            "select-profile" -> commandSelectProfile(account)
            "add-feed" -> {
                val url = args.getOrNull(1) ?: error("Usage: add-feed <url>")
                commandAddFeed(account, url)
            }
            "feeds" -> commandFeeds(account)
            "articles" -> commandArticles(account)
            else -> {
                System.err.println("Unknown command: $command")
                printUsage()
            }
        }
    }

    exitProcess(0)
}

private fun printUsage() {
    println("""
        Usage: bench <command> [args]

        Commands:
          refresh           Sync all feeds
          refresh-profile   Sync with network vs query breakdown
          add-feed <url>    Add a feed by URL
          feeds             List all feeds
          articles          Count articles by status
          login             Verify credentials only (no account write)
          reset             Delete bench/data/ and start fresh
    """.trimIndent())
}
