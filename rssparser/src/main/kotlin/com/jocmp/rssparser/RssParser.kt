package com.jocmp.rssparser

import com.jocmp.rssparser.internal.Fetcher
import com.jocmp.rssparser.internal.Parser
import com.jocmp.rssparser.internal.ParserInput
import com.jocmp.rssparser.model.RssChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import java.nio.charset.Charset
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
    suspend fun parse(rawRssFeed: String, charset: Charset? = null): RssChannel =
        withContext(coroutineContext) {
            val parserInput = generateParserInputFromString(rawRssFeed, charset)
            return@withContext parser.parse(parserInput)
        }

    private fun generateParserInputFromString(rawRssFeed: String, charset: Charset?): ParserInput {
        val cleanedXml = rawRssFeed.trim()
        val inputStream = cleanedXml.byteInputStream(charset ?: Charsets.UTF_8)
        return ParserInput.from(inputStream, charset)
    }
}
