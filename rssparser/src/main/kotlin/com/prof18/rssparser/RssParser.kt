package com.prof18.rssparser

import com.prof18.rssparser.internal.Fetcher
import com.prof18.rssparser.internal.Parser
import com.prof18.rssparser.internal.ParserInput
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class RssParser internal constructor(
    private val fetcher: Fetcher,
    private val parser: Parser,
) {
    private val coroutineContext: CoroutineContext =
        SupervisorJob() + Dispatchers.Default

    internal interface Builder {
        fun build(): RssParser
    }

    /**
     * Downloads and parses an RSS feed from an [url] and returns an [RssChannel].
     */
    suspend fun getRssChannel(url: String): RssChannel = withContext(coroutineContext) {
        val parserInput = fetcher.fetch(url)
        return@withContext parser.parse(parserInput)
    }

    /**
     * Parses an RSS feed provided by [rawRssFeed] and returns an [RssChannel]
     */
    suspend fun parse(rawRssFeed: String): RssChannel = withContext(coroutineContext) {
        val parserInput = generateParserInputFromString(rawRssFeed)
        return@withContext parser.parse(parserInput)
    }

    private fun generateParserInputFromString(rawRssFeed: String): ParserInput {
        val cleanedXml = rawRssFeed.trim()
        val inputStream = cleanedXml.byteInputStream(Charsets.UTF_8)
        return ParserInput.from(inputStream)
    }
}
