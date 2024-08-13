package com.prof18.rssparser.internal.atom

import com.prof18.rssparser.internal.AtomKeyword
import com.prof18.rssparser.internal.ChannelFactory
import com.prof18.rssparser.internal.FeedHandler
import com.prof18.rssparser.model.RssChannel
import org.xml.sax.Attributes

internal class AtomFeedHandler : FeedHandler {

    private var isInsideItem = false
    private var isInsideChannel = true

    private var channelFactory = ChannelFactory()

    override fun onStartRssElement(qName: String?, attributes: Attributes?) {
        when (qName) {
            AtomKeyword.Atom.value -> isInsideChannel = true
            AtomKeyword.Entry.Item.value -> isInsideItem = true

            AtomKeyword.Entry.Category.value -> {
                if (isInsideItem) {
                    val category = attributes?.getValue(AtomKeyword.Entry.Term.value)
                    channelFactory.articleBuilder.addCategory(category)
                }
            }

            AtomKeyword.Link.value -> {
                val href = attributes?.getValue(AtomKeyword.Link.Href.value)
                val rel = attributes?.getValue(AtomKeyword.Link.Rel.value)
                if (rel != AtomKeyword.Link.Edit.value && rel != AtomKeyword.Link.Self.value) {
                    when {
                        isInsideItem -> channelFactory.articleBuilder.link(href)
                        else -> channelFactory.channelBuilder.link(href)
                    }
                }
            }
        }
    }

    override fun endElement(qName: String?, text: String) {
        when {
            isInsideItem -> {
                when (qName) {
                    AtomKeyword.Entry.Published.value -> channelFactory.articleBuilder.pubDate(text)
                    AtomKeyword.Updated.value -> channelFactory.articleBuilder.pubDateIfNull(text)
                    AtomKeyword.Title.value -> channelFactory.articleBuilder.title(text)
                    AtomKeyword.Entry.Author.value -> channelFactory.articleBuilder.author(text)
                    AtomKeyword.Entry.Guid.value -> channelFactory.articleBuilder.guid(text)
                    AtomKeyword.Entry.Content.value -> {
                        channelFactory.articleBuilder.content(text)
                        channelFactory.setImageFromContent(text)
                    }
                    AtomKeyword.Entry.Description.value -> {
                        channelFactory.articleBuilder.description(text)
                        channelFactory.setImageFromContent(text)
                    }
                    AtomKeyword.Entry.Category.value -> {
                        if (isInsideItem) {
                            if (text.isNotEmpty()) {
                                channelFactory.articleBuilder.addCategory(text)
                            }
                        }
                    }
                    AtomKeyword.Entry.Item.value -> {
                        channelFactory.buildArticle()
                        isInsideItem = false
                    }
                }
            }

            isInsideChannel -> {
                when (qName) {
                    AtomKeyword.Icon.value -> channelFactory.channelImageBuilder.url(text)
                    AtomKeyword.Updated.value -> channelFactory.channelBuilder.lastBuildDate(text)
                    AtomKeyword.Subtitle.value -> channelFactory.channelBuilder.description(text)
                    AtomKeyword.Title.value -> channelFactory.channelBuilder.title(text)
                    AtomKeyword.Atom.value -> isInsideChannel = false
                }
            }
        }
    }

    override fun buildRssChannel(): RssChannel =
        channelFactory.build()
}
