package com.jocmp.rssparser

import com.jocmp.rssparser.internal.FetchResponse
import com.jocmp.rssparser.internal.Fetcher
import com.jocmp.rssparser.model.ConditionalGetInfo
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class MalformedFeedParserTest {
    @Test
    fun whenReceivingAMalformedXmlTheParserWillHandleIt() = runTest {
        val rssParser = RssParser(
            fetcher = object : Fetcher {
                override suspend fun fetch(url: String, conditionalGet: ConditionalGetInfo): FetchResponse =
                    FetchResponse(
                        parserInput = readFileFromResources("feed-test-malformed.xml"),
                        conditionalGet = ConditionalGetInfo.EMPTY,
                    )
            },
            parser = ParserFactory.build()
        )

        val channel = rssParser.getRssChannel("feed-url")
        assertTrue(channel.items.isNotEmpty())
    }
}
