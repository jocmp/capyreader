package com.jocmp.feedfinder.sources

import com.jocmp.feedfinder.parser.Feed

internal sealed interface Source {
    suspend fun find(): List<Feed>
}
