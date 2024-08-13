package com.prof18.rssparser.internal

import com.prof18.rssparser.exception.RssParsingException
import com.prof18.rssparser.internal.atom.AtomFeedHandler
import com.prof18.rssparser.internal.rss.RssFeedHandler
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.internal.closeQuietly
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream
import java.nio.charset.Charset
import javax.xml.parsers.SAXParserFactory

internal class JvmXmlParser(
    private val charset: Charset? = null,
    private val dispatcher: CoroutineDispatcher,
) : XmlParser {
    override suspend fun parseXML(input: ParserInput): RssChannel = withContext(dispatcher) {
        try {
            val parser = SAXParserFactory.newInstance().newSAXParser()
            val handler = SaxFeedHandler()

            if (charset != null) {
                val inputSource = InputSource(input.inputStream).apply {
                    encoding = charset.toString()
                }
                parser.parse(inputSource, handler)
            } else {
                parser.parse(input.inputStream, handler)
            }

            return@withContext handler.getChannel()
        } catch (exception: SAXParseException) {
            throw RssParsingException(
                message = "Something went wrong during the parsing of the feed. Please double check if the XML is valid",
                cause = exception
            )
        } finally {
            input.inputStream.closeQuietly()
        }
    }

    override fun generateParserInputFromString(rawRssFeed: String): ParserInput {
        val cleanedXml = rawRssFeed.trim()
        val inputStream: InputStream = cleanedXml.byteInputStream(charset ?: Charsets.UTF_8)
        return ParserInput(inputStream)
    }
}

private class SaxFeedHandler : DefaultHandler() {
    private var feedHandler: FeedHandler? = null
    private val textBuilder: StringBuilder = StringBuilder()

    fun getChannel(): RssChannel =
        requireNotNull(feedHandler).buildRssChannel()

    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
    ) {
        textBuilder.setLength(0)

        when (qName) {
            RssKeyword.Rss.value -> {
                feedHandler = RssFeedHandler()
            }
            AtomKeyword.Atom.value -> {
                feedHandler = AtomFeedHandler()
            }
            else -> feedHandler?.onStartRssElement(qName, attributes)
        }
    }

    override fun endElement(
        uri: String?,
        localName: String?,
        qName: String?,
    ) {
        val text = textBuilder.toString().trim()
        feedHandler?.endElement(qName, text)
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        textBuilder.append(String(ch, start, length))
    }
}
