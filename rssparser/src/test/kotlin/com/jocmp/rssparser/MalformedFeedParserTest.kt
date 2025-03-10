package com.jocmp.rssparser

import com.jocmp.rssparser.internal.Fetcher
import com.jocmp.rssparser.internal.ParserInput
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class MalformedFeedParserTest {
    @Test
    fun whenReceivingAMalformedXmlTheParserWillHandleIt() = runTest {
        val rssParser = RssParser(
            fetcher = object : Fetcher {
                override suspend fun fetch(url: String): ParserInput =
                    readFileFromResources("feed-test-malformed.xml")
            },
            parser = ParserFactory.build()
        )

        val channel = rssParser.getRssChannel("feed-url")
        assertTrue(channel.items.isNotEmpty())
    }
}
