package com.jocmp.feedfinder.source

import com.jocmp.feedfinder.Feed
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File

class TestSource(private val documentPath: String): Source {
    override val document: Document?
        get() {
            val file = File("src/test/resources/$documentPath")
            return Jsoup.parse(file)
        }

    override fun find(): List<Feed> {
        throw NotImplementedError("No base implementation")
    }
}
