package com.jocmp.rssparser.rss

import com.jocmp.rssparser.ParserFactory
import com.jocmp.rssparser.readFileFromResources
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class XmlParserRssItsFossTest {
    @Test
    fun channelTitle_decodesApostrophe() = runTest {
        val input = readFileFromResources("feed-rss-itsfoss.xml")
        val channel = ParserFactory.build().parse(input)

        assertEquals("It's FOSS", channel.title)
    }

    @Test
    fun articleTitle_decodesNumericEntities() = runTest {
        val input = readFileFromResources("feed-rss-itsfoss.xml")
        val channel = ParserFactory.build().parse(input)
        val article = channel.items.first { it.title?.contains("GrapheneOS") == true }

        assertEquals(
            "Tired of Google\u0027s Tracking? Motorola\u0027s GrapheneOS-Powered Phones Are Coming",
            article.title,
        )
    }
}
