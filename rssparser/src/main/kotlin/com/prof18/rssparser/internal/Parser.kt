package com.prof18.rssparser.internal

import com.prof18.rssparser.model.RssChannel

internal interface Parser {
    suspend fun parse(input: ParserInput): RssChannel
}
