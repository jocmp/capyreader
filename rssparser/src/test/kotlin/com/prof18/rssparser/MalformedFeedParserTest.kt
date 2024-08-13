package com.prof18.rssparser

import com.prof18.rssparser.internal.XmlFetcher
import com.prof18.rssparser.internal.ParserInput
import com.prof18.rssparser.RssParser
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class MalformedFeedParserTest : XmlParserTestExecutor() {

    @Test
    fun whenReceivingAMalformedXmlTheParserWillHandleIt() = runTest {
        val rssParser = RssParser(
            xmlFetcher = object : XmlFetcher {
                override suspend fun fetchXml(url: String): ParserInput =
                    com.prof18.rssparser.readFileFromResources("feed-test-malformed.xml")

                override suspend fun fetchXmlAsString(url: String): String =
                    com.prof18.rssparser.readFileFromResourcesAsString("feed-test-malformed.xml")
            },
            xmlParser = XmlParserFactory.createXmlParser()
        )

        val channel = rssParser.getRssChannel("feed-url")
        assertTrue(channel.items.isNotEmpty())
    }
}
