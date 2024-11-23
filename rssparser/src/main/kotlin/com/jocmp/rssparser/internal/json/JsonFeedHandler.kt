package com.jocmp.rssparser.internal.json

import com.jocmp.rssparser.internal.ChannelFactory
import com.jocmp.rssparser.internal.FeedHandler
import com.jocmp.rssparser.internal.json.models.Feed
import com.jocmp.rssparser.internal.json.models.Item
import com.jocmp.rssparser.model.RssChannel
import com.jocmp.rssparser.model.RssImage

internal class JsonFeedHandler(private val feed: Feed) : FeedHandler {
    private var channelFactory = ChannelFactory()

    override fun build(): RssChannel {
        channel()

        feed.items.forEach { item(it) }

        return channelFactory.build()
    }

    private fun channel() {
        channelFactory.channelBuilder.apply {
            title(feed.title)
            link(feed.home_page_url)
            description(feed.description)
            image(RssImage(url = feed.icon))
        }
    }

    private fun item(item: Item) {
        channelFactory.articleBuilder.apply {
            title(item.title)
            link(item.url)
            description(item.summary)
            content(item.content_html ?: item.content_text)
            pubDate(item.date_published)
            image(item.image)
        }

        channelFactory.buildArticle()
    }
}
