package com.jocmp.feedfinder.sources

import com.jocmp.feedfinder.parser.Feed

internal class BodyLinkSource: Source {
    override suspend fun find(): List<Feed> {
        return emptyList()
    }
}
