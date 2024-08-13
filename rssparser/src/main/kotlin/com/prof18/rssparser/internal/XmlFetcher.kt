package com.prof18.rssparser.internal

internal interface XmlFetcher {
    suspend fun fetchXml(url: String): ParserInput
    suspend fun fetchXmlAsString(url: String): String
}
