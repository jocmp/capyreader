package com.prof18.rssparser.internal.atom

import com.prof18.rssparser.internal.AtomKeyword.*
import com.prof18.rssparser.internal.ChannelFactory
import com.prof18.rssparser.internal.FeedHandler
import com.prof18.rssparser.model.RssChannel
import org.jsoup.nodes.Element

internal class AtomFeedHandler(val atom: Element) : FeedHandler {
    private var channelFactory = ChannelFactory()

    override fun build(): RssChannel {
        atom.children().forEach { node ->
            when (node.tagName()) {
                Icon.value -> channelFactory.channelImageBuilder.url(node.text())
                Updated.value -> channelFactory.channelBuilder.lastBuildDate(node.text())
                Subtitle.value -> channelFactory.channelBuilder.description(
                    node.text().trim()
                )

                Title.value -> channelFactory.channelBuilder.title(
                    node.wholeText().trim()
                )

                Link.value -> withLink(node) { href ->
                    channelFactory.channelBuilder.link(href)
                }

                Entry.Tag.value -> entry(node)
            }
        }

        return channelFactory.build()
    }


    private fun entry(entry: Element) {
        entry.children().forEach { node ->
            when (node.tagName()) {
                Link.value -> withLink(node) { href ->
                    channelFactory.articleBuilder.link(href)
                }

                Entry.Published.value -> {
                    channelFactory.articleBuilder.pubDate(node.text())
                }

                Updated.value -> channelFactory.articleBuilder.pubDateIfNull(node.text())
                Title.value -> channelFactory.articleBuilder.title(
                    node.wholeText().trim()
                )

                Entry.Author.Author.value -> {
                    val name = node.children()
                        .firstOrNull { it.tagName() == Entry.Author.Name.value }

                    if (name != null) {
                        channelFactory.articleBuilder.author(name.text().trim())
                    }
                }

                Entry.Guid.value -> channelFactory.articleBuilder.guid(node.text())
                Entry.Content.value -> {
                    val text = node.wholeText()

                    channelFactory.articleBuilder.content(text.trim())
                    channelFactory.setImageFromContent(text)
                }

                Entry.Description.value -> {
                    channelFactory.articleBuilder.description(node.wholeText().trim())
                    channelFactory.setImageFromContent(node.wholeText())
                }

                Entry.Category.value -> {
                    val text = node.attr(Entry.Term.value)
                    if (text.isNotEmpty()) {
                        channelFactory.articleBuilder.addCategory(text)
                    }
                }
            }
        }

        channelFactory.buildArticle()
    }

    private fun withLink(element: Element, callback: (href: String) -> Unit) {
        val href = element.attr(Link.Href.value)
        val rel = element.attr(Link.Rel.value)

        if (!filteredRelations.contains(rel)) {
            callback(href)
        }
    }

    companion object {
        val filteredRelations = listOf(
            Link.Edit.value,
            Link.Self.value,
            Link.Replies.value,
        )
    }
}
