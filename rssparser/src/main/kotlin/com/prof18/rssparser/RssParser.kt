package com.prof18.rssparser

import com.prof18.rssparser.exception.RssParsingException
import com.prof18.rssparser.internal.XmlFetcher
import com.prof18.rssparser.internal.XmlParser
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class RssParser internal constructor(
    private val xmlFetcher: XmlFetcher,
    private val xmlParser: XmlParser,
) {

    private val coroutineContext: CoroutineContext =
        SupervisorJob() + Dispatchers.Default

    internal interface Builder {
        /**
         * Creates a [RssParser] object
         */
        fun build(): RssParser
    }

    /**
     * Downloads and parses an RSS feed from an [url] and returns an [RssChannel].
     *
     * If the parsing fails because the XML is malformed, it will re-download the XML as a string,
     * clean it up and try to parse it again. If it fails again, it will throw an [RssParsingException].
     */
    suspend fun getRssChannel(url: String): RssChannel = withContext(coroutineContext) {
        val parserInput = xmlFetcher.fetchXml(url)
        return@withContext try {
            xmlParser.parseXML(parserInput)
        } catch (_: RssParsingException) {
            val xmlAsString = xmlFetcher.fetchXmlAsString(url)
            val input = xmlParser.generateParserInputFromString(xmlAsString)
            xmlParser.parseXML(input)
        }
    }

    /**
     * Parses an RSS feed provided by [rawRssFeed] and returns an [RssChannel]
     */
    suspend fun parse(rawRssFeed: String): RssChannel = withContext(coroutineContext) {
        val parserInput = xmlParser.generateParserInputFromString(rawRssFeed)
        return@withContext xmlParser.parseXML(parserInput)
    }
}
