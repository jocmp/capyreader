package com.prof18.rssparser.internal

import com.prof18.rssparser.model.RssChannel
import org.xml.sax.Attributes

internal interface FeedHandler {
    fun onStartRssElement(
        qName: String?,
        attributes: Attributes?,
    )

    fun endElement(
        qName: String?,
        text: String,
    )

    fun buildRssChannel(): RssChannel
}
