package com.jocmp.feedfinder.source

import com.jocmp.feedfinder.Feed
import org.jsoup.nodes.Document

class EmptySource: Source {
    override val document: Document? = null

    override fun find(): List<Feed> {
        throw NotImplementedError("No base implementation")
    }
}
