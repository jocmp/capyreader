package com.prof18.rssparser.internal

import com.prof18.rssparser.model.RssChannel
import org.xml.sax.Attributes

internal interface FeedHandler {
    fun build(): RssChannel
}
