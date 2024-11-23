package com.jocmp.rssparser.internal

import com.jocmp.rssparser.model.RssChannel

internal interface Parser {
    suspend fun parse(input: ParserInput): RssChannel
}
