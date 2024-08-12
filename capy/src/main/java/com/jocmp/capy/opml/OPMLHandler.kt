package com.jocmp.capy.opml

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import java.io.InputStream

internal object OPMLHandler {
    fun parse(inputStream: InputStream): List<Outline> {
        val res = Jsoup.parse(inputStream, "UTF-8", "", Parser.xmlParser())

        return res.getElementsByTag("body").flatMap { body ->
            body
                .select("> outline")
                .map { element ->
                    if (element.isFeed) {
                        Outline.FeedOutline(element.toFeed)
                    } else {
                        Outline.FolderOutline(element.toFolder)
                    }
                }
        }
    }
}

private val Element.toFeed
    get() = Feed(
        title = attr("title"),
        text = attr("text"),
        htmlUrl = attr("htmlUrl"),
        xmlUrl = attr("xmlUrl"),
    )

private val Element.toFolder: Folder
    get() {
        val elements = children().map { child ->
            if (child.isFeed) {
                child.toFeed
            } else {
                child.toFolder
            }
        }

        val feeds = elements.filterIsInstance<Feed>().toMutableList()
        val folders = elements.filterIsInstance<Folder>().toMutableList()

        return Folder(
            title = attr("title"),
            text = attr("text"),
            feeds = feeds,
            folders = folders
        )
    }

private val Element.isFeed
    get() = attr("xmlUrl").isNotBlank()
