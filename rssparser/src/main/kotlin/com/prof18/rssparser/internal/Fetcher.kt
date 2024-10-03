package com.prof18.rssparser.internal

internal interface Fetcher {
    suspend fun fetch(url: String): ParserInput
}
