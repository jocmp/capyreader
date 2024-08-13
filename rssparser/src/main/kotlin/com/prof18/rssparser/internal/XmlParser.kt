package com.prof18.rssparser.internal

import com.prof18.rssparser.model.RssChannel

internal interface XmlParser {
    suspend fun parseXML(input: ParserInput): RssChannel
    fun generateParserInputFromString(rawRssFeed: String): ParserInput
}
