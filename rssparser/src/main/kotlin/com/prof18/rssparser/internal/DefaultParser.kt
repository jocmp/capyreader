package com.prof18.rssparser.internal

import com.prof18.rssparser.exception.RssParsingException
import com.prof18.rssparser.internal.atom.AtomFeedHandler
import com.prof18.rssparser.internal.atom.AtomKeyword
import com.prof18.rssparser.internal.json.JsonFeedHandler
import com.prof18.rssparser.internal.json.models.Feed
import com.prof18.rssparser.internal.rdf.RdfFeedHandler
import com.prof18.rssparser.internal.rdf.RdfKeyword
import com.prof18.rssparser.internal.rss.RssFeedHandler
import com.prof18.rssparser.internal.rss.RssKeyword
import com.prof18.rssparser.model.RssChannel
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okio.IOException
import okio.buffer
import okio.source
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser as JsoupParser

internal class DefaultParser(
    private val dispatcher: CoroutineDispatcher,
) : Parser {
    override suspend fun parse(input: ParserInput): RssChannel {
        return withContext(dispatcher) {

            val handler = findHandler(input)

            if (handler == null) {
                throw RssParsingException(
                    message = "Could not find top-level RSS node",
                    cause = null
                )
            }

            handler.build()
        }
    }

    private fun findHandler(input: ParserInput): FeedHandler? {
        val document = tryXmlParse(input) ?: return null

        val handler = document.children().firstNotNullOfOrNull { node ->
            when (node.tagName()) {
                RssKeyword.Rss.value -> {
                    RssFeedHandler(document)
                }

                AtomKeyword.Atom.value -> {
                    AtomFeedHandler(node)
                }

                RdfKeyword.Rdf.value -> {
                    RdfFeedHandler(node)
                }

                else -> tryParseJson(input)
            }
        }

        return handler ?: tryParseJson(input)
    }
}

private fun tryXmlParse(input: ParserInput): Document? {
    return try {
        Jsoup.parse(input.inputStream(), null, "", JsoupParser.xmlParser())
    } catch (e: IOException) {
        null
    }
}

@OptIn(ExperimentalStdlibApi::class)
private fun tryParseJson(input: ParserInput): FeedHandler? {
    return try {
        val moshi = Moshi
            .Builder()
            .build()

        val feed = moshi.adapter<Feed>()
            .fromJson(input.inputStream().source().buffer()) ?: return null

        JsonFeedHandler(feed)
    } catch (e: IOException) {
        null
    }
}
