package com.prof18.rssparser.internal.atom

import com.prof18.rssparser.internal.AtomKeyword
import com.prof18.rssparser.internal.ChannelFactory
import com.prof18.rssparser.internal.FeedHandler
import com.prof18.rssparser.model.RssChannel
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

internal class AtomFeedHandler(val document: Document) : FeedHandler {
    private var channelFactory = ChannelFactory()

    override fun build(): RssChannel {
        channel()

        return channelFactory.build()
    }

    private fun channel() {
        withDocument(AtomKeyword.Atom.value) { node ->
            when (node.tagName()) {
                AtomKeyword.Icon.value -> channelFactory.channelImageBuilder.url(node.text())
                AtomKeyword.Updated.value -> channelFactory.channelBuilder.lastBuildDate(node.text())
                AtomKeyword.Subtitle.value -> channelFactory.channelBuilder.description(
                    node.text().trim()
                )

                AtomKeyword.Title.value -> channelFactory.channelBuilder.title(
                    node.wholeText().trim()
                )

                AtomKeyword.Link.value -> withLink(node) { href ->
                    channelFactory.channelBuilder.link(href)
                }

                AtomKeyword.Entry.Item.value -> {
                    entry(node)
                }
            }
        }
    }

    private fun entry(entry: Element) {
        entry.children().forEach { node ->

            when (node.tagName()) {
                AtomKeyword.Link.value -> withLink(node) { href ->
                    channelFactory.articleBuilder.link(href)
                }

                AtomKeyword.Entry.Published.value -> {
                    channelFactory.articleBuilder.pubDate(node.text())
                }

                AtomKeyword.Updated.value -> channelFactory.articleBuilder.pubDateIfNull(node.text())
                AtomKeyword.Title.value -> channelFactory.articleBuilder.title(
                    node.wholeText().trim()
                )

                AtomKeyword.Entry.Author.Author.value -> {
                    val name = node.children()
                        .firstOrNull { it.tagName() == AtomKeyword.Entry.Author.Name.value }

                    if (name != null) {
                        channelFactory.articleBuilder.author(name.text().trim())
                    }
                }

                AtomKeyword.Entry.Guid.value -> channelFactory.articleBuilder.guid(node.text())
                AtomKeyword.Entry.Content.value -> {
                    val text = node.wholeText()

                    channelFactory.articleBuilder.content(text.trim())
                    channelFactory.setImageFromContent(text)
                }

                AtomKeyword.Entry.Description.value -> {
                    channelFactory.articleBuilder.description(node.wholeText().trim())
                    channelFactory.setImageFromContent(node.wholeText())
                }

                AtomKeyword.Entry.Category.value -> {
                    val text = node.attr(AtomKeyword.Entry.Term.value)
                    if (text.isNotEmpty()) {
                        channelFactory.articleBuilder.addCategory(text)
                    }
                }
            }
        }

        channelFactory.buildArticle()
    }

    private fun withLink(element: Element, callback: (href: String) -> Unit) {
        val href = element.attr(AtomKeyword.Link.Href.value)
        val rel = element.attr(AtomKeyword.Link.Rel.value)
        if (rel != AtomKeyword.Link.Edit.value && rel != AtomKeyword.Link.Self.value) {
            callback(href)
        }
    }

    private fun withDocument(selector: String, onElement: (element: Element) -> Unit) {
        document.select(selector).first()?.children()?.forEach(onElement)
    }
}
