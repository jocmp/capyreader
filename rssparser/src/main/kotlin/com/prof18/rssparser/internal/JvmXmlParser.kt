package com.prof18.rssparser.internal

import com.prof18.rssparser.exception.RssParsingException
import com.prof18.rssparser.internal.rss.RssFeedHandler
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.internal.closeQuietly
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.xml.sax.SAXParseException
import java.io.InputStream
import java.nio.charset.Charset

internal class JvmXmlParser(
    private val charset: Charset? = null,
    private val dispatcher: CoroutineDispatcher,
) : XmlParser {
    override suspend fun parseXML(input: ParserInput): RssChannel {
        return withContext(dispatcher) {
            try {
                val document = Jsoup.parse(input.inputStream,null, "", Parser.xmlParser())

                return@withContext RssFeedHandler(document).build()
            } catch (exception: SAXParseException) {
                throw RssParsingException(
                    message = "Something went wrong during the parsing of the feed. Please double check if the XML is valid",
                    cause = exception
                )
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
//
//private class SaxFeedHandler : DefaultHandler() {
//    private var feedHandler: FeedHandler? = null
//    private val textBuilder: StringBuilder = StringBuilder()
//
//    fun getChannel(): RssChannel =
//        requireNotNull(feedHandler).buildRssChannel()
//
//    override fun startElement(
//        uri: String?,
//        localName: String?,
//        qName: String?,
//        attributes: Attributes?,
//    ) {
//        textBuilder.setLength(0)
//
//        when (qName) {
//            RssKeyword.Rss.value -> {
//                feedHandler = RssFeedHandler()
//            }
//            AtomKeyword.Atom.value -> {
//                feedHandler = AtomFeedHandler()
//            }
//            else -> feedHandler?.onStartRssElement(qName, attributes)
//        }
//    }
//
//    override fun endElement(
//        uri: String?,
//        localName: String?,
//        qName: String?,
//    ) {
//        val text = textBuilder.toString().trim()
//        feedHandler?.endElement(qName, text)
//    }
//
//    override fun characters(ch: CharArray, start: Int, length: Int) {
//        textBuilder.append(String(ch, start, length))
//    }
//}
