package com.jocmp.rssparser.internal

import com.jocmp.rssparser.model.ConditionalGetInfo

internal interface Fetcher {
    suspend fun fetch(url: String): ParserInput = fetch(url, ConditionalGetInfo.EMPTY).parserInput
        ?: error("Conditional GET not expected here")

    suspend fun fetch(url: String, conditionalGet: ConditionalGetInfo): FetchResponse
}
