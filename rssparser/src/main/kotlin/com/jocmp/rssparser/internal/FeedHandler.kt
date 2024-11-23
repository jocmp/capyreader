package com.jocmp.rssparser.internal

import com.jocmp.rssparser.model.RssChannel

internal interface FeedHandler {
    fun build(): RssChannel
}
