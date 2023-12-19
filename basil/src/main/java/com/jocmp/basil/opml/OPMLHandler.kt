package com.jocmp.basil.opml

import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import javax.xml.parsers.SAXParserFactory

internal class OPMLHandler : DefaultHandler() {
    lateinit var opmlDocument: OPMLDocument
    private var currentValue: StringBuilder? = null
    private var currentFolder: Folder? = null
    private var currentType: OutlineType? = null

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
        when (qName) {
            OUTLINE -> {
                if (attributes.getValue("type") == "rss") {
                    currentType = OutlineType.FEED
                    currentFolder?.also { folder ->
                        folder.feeds.add(
                            Feed(
                                id = attributes.getValue("basil_id"),
                                title = attributes.getValue("title"),
                                text = attributes.getValue("text"),
                                htmlUrl = attributes.getValue("htmlUrl"),
                                xmlUrl = attributes.getValue("xmlUrl"),
                            )
                        )
                    } ?: run {
                        opmlDocument.outlines.add(
                            Outline.FeedOutline(
                                Feed(
                                    id = attributes.getValue("basil_id"),
                                    title = attributes.getValue("title"),
                                    text = attributes.getValue("text"),
                                    htmlUrl = attributes.getValue("htmlUrl"),
                                    xmlUrl = attributes.getValue("xmlUrl"),
                                )
                            )
                        )
                    }
                } else {
                    currentType = OutlineType.FOLDER
                    currentFolder = Folder(
                        title = attributes.getValue("title"),
                        text = attributes.getValue("text")
                    )
                    currentValue = StringBuilder()
                }
            }
        }
    }

    @Throws(SAXException::class)
    override fun endElement(uri: String, localName: String, qName: String) {
        when (qName) {
            OUTLINE -> {
                if (currentType == OutlineType.FOLDER) {
                    currentFolder?.let { folder ->
                        opmlDocument.outlines.add(Outline.FolderOutline(folder))
                    }
                    currentFolder = null
                    currentType = null
                } else if (currentType === OutlineType.FEED && currentFolder != null) {
                    currentType = OutlineType.FOLDER
                } else {
                    currentType = null
                }
            }
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
    }
}

private enum class OutlineType {
    FOLDER,
    FEED,
}
