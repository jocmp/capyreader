package com.prof18.rssparser

import com.prof18.rssparser.internal.ParserInput
import com.prof18.rssparser.internal.XmlFetcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class MalformedFeedParserTest {
    @Test
    fun whenReceivingAMalformedXmlTheParserWillHandleIt() = runTest {
        val rssParser = RssParser(
            xmlFetcher = object : XmlFetcher {
                override suspend fun fetchXml(url: String): ParserInput =
                    readFileFromResources("feed-test-malformed.xml")

                override suspend fun fetchXmlAsString(url: String): String =
                    readFileFromResourcesAsString("feed-test-malformed.xml")
            },
            xmlParser = XmlParserFactory.createXmlParser()
        )

        val channel = rssParser.getRssChannel("feed-url")
        assertTrue(channel.items.isNotEmpty())
    }
}
