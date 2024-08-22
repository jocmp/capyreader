package com.prof18.rssparser.internal

import com.prof18.rssparser.exception.RssParsingException
import com.prof18.rssparser.internal.atom.AtomFeedHandler
import com.prof18.rssparser.internal.rss.RssFeedHandler
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.internal.closeQuietly
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.io.InputStream
import java.nio.charset.Charset

internal class DefaultXmlParser(
    private val charset: Charset? = null,
    private val dispatcher: CoroutineDispatcher,
) : XmlParser {
    override suspend fun parseXML(input: ParserInput): RssChannel {
        return withContext(dispatcher) {
            try {
                val document = Jsoup.parse(input.inputStream, null, "", Parser.xmlParser())

                val handler = document.children().firstNotNullOfOrNull { node ->
                    when (node.tagName()) {
                        RssKeyword.Rss.value -> {
                            RssFeedHandler(document)
                        }

                        AtomKeyword.Atom.value -> {
                            AtomFeedHandler(document)
                        }

                        else -> null
                    }
                }

                if (handler == null) {
                    throw RssParsingException(
                        message = "Could not find top-level RSS node",
                        cause = null
                    )
                }

                handler.build()
            } finally {
                input.inputStream.closeQuietly()
            }
        }
    }

    override fun generateParserInputFromString(rawRssFeed: String): ParserInput {
        val cleanedXml = rawRssFeed.trim()
        val inputStream: InputStream = cleanedXml.byteInputStream(charset ?: Charsets.UTF_8)
        return ParserInput(inputStream)
    }
}
