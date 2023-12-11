package com.jocmp.feedfinder.sources

import com.jocmp.feedfinder.Response
import com.jocmp.feedfinder.parser.Feed
import com.jocmp.feedfinder.parser.Parser
import org.jsoup.nodes.Document
import java.net.URL

internal sealed interface Source {
    suspend fun find(): List<Feed>
}
