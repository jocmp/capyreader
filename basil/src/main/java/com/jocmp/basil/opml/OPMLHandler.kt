package com.jocmp.basil.opml

import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream
import javax.xml.parsers.SAXParserFactory

internal class OPMLHandler : DefaultHandler() {
    lateinit var opmlDocument: OPMLDocument
    private var currentValue: StringBuilder? = null
    private var outlines = ArrayDeque<Outline>()

    @Throws(SAXException::class)
    override fun characters(ch: CharArray, start: Int, length: Int) {
        if (currentValue == null) {
            currentValue = StringBuilder()
        } else {
            currentValue!!.appendRange(ch, start, start + length)
        }
    }

    @Throws(SAXException::class)
    override fun startDocument() {
        opmlDocument = OPMLDocument()
    }

    @Throws(SAXException::class)
    override fun startElement(
        uri: String,
        localName: String,
        qName: String,
        attributes: Attributes
    ) {
        if (qName != OUTLINE) {
            return
        }

        if (attributes.getValue("type") == "rss") {
            val feed = Feed(
                id = attributes.getValue("basil_id"),
                title = attributes.getValue("title"),
                text = attributes.getValue("text"),
                htmlUrl = attributes.getValue("htmlUrl"),
                xmlUrl = attributes.getValue("xmlUrl"),
            )

            outlines.add(Outline.FeedOutline(feed))
        } else {
            val folder = Folder(
                title = attributes.getValue("title"),
                text = attributes.getValue("text")
            )

            outlines.add(Outline.FolderOutline(folder))
        }
    }

    @Throws(SAXException::class)
    override fun endElement(uri: String, localName: String, qName: String) {
        if (qName != OUTLINE) {
            return
        }

        val outline = outlines.removeLastOrNull() ?: return

        if (outlines.isEmpty()) {
            opmlDocument.outlines.add(outline)
        }

        val parentFolder = (outlines.lastOrNull() as? Outline.FolderOutline)?.folder ?: return

        if (outline is Outline.FeedOutline) {
            parentFolder.feeds.add(outline.feed)
        } else if (outline is Outline.FolderOutline) {
            parentFolder.folders.add(outline.folder)
        }
    }

    companion object {
        private const val OUTLINE = "outline"

        fun parse(filePath: String): List<Outline> {
            val handler = OPMLHandler()

            SAXParserFactory
                .newInstance()
                .newSAXParser()
                .parse(filePath, handler)

            return handler.opmlDocument.outlines
        }

        fun parse(inputStream: InputStream): List<Outline> {
            val handler = OPMLHandler()

            SAXParserFactory
                .newInstance()
                .newSAXParser()
                .parse(inputStream, handler)

            return handler.opmlDocument.outlines
        }
    }
}
