package com.jocmp.rssparser.atom

import com.jocmp.rssparser.ParserFactory
import com.jocmp.rssparser.readFileFromResources
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class XmlParserAtomHtmlTitleEntitiesTest {
    @Test
    fun channelTitle_decodesHtmlEntities() = runTest {
        val input = readFileFromResources("feed-atom-html-title-entities.xml")
        val channel = ParserFactory.build().parse(input)

        assertEquals("Attack & Defense", channel.title)
    }

    @Test
    fun articleTitle_decodesHtmlEntities() = runTest {
        val input = readFileFromResources("feed-atom-html-title-entities.xml")
        val channel = ParserFactory.build().parse(input)

        assertEquals(
            "Firefox Security & Privacy Newsletter 2025 Q4",
            channel.items[1].title,
        )
    }
}
