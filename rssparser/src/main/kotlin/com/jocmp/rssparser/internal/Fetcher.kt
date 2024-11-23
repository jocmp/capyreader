package com.jocmp.rssparser.internal

internal interface Fetcher {
    suspend fun fetch(url: String): ParserInput
}
