package com.jocmp.rssparser.internal.atom

import com.jocmp.rssparser.internal.ChannelFactory
import com.jocmp.rssparser.internal.FeedHandler
import com.jocmp.rssparser.internal.atom.AtomKeyword.Entry
import com.jocmp.rssparser.internal.atom.AtomKeyword.Icon
import com.jocmp.rssparser.internal.atom.AtomKeyword.Link
import com.jocmp.rssparser.internal.atom.AtomKeyword.Subtitle
import com.jocmp.rssparser.internal.atom.AtomKeyword.Title
import com.jocmp.rssparser.internal.atom.AtomKeyword.Updated
import com.jocmp.rssparser.model.RssChannel
import org.jsoup.nodes.Element

internal class AtomFeedHandler(val atom: Element) : FeedHandler {
    private var channelFactory = ChannelFactory()

    override fun build(): RssChannel {
        atom.children().forEach { node ->
            when (node.tagName()) {
                Icon() -> channelFactory.channelImageBuilder.url(node.text())
                Updated() -> channelFactory.channelBuilder.lastBuildDate(node.text())
                Subtitle() -> channelFactory.channelBuilder.description(
                    node.text().trim()
                )

                Title() -> channelFactory.channelBuilder.title(
                    node.wholeText().trim()
                )

                Link() -> withLink(node) { href ->
                    channelFactory.channelBuilder.link(href)
                }

                Entry.Tag() -> entry(node)
            }
        }

        return channelFactory.build()
    }


    private fun entry(entry: Element) {
        entry.children().forEach { node ->
            when (node.tagName()) {
                Link() -> withLink(node) { href ->
                    channelFactory.articleBuilder.link(href)
                }

                Entry.Published() -> {
                    channelFactory.articleBuilder.pubDate(node.text())
                }

                Updated() -> channelFactory.articleBuilder.pubDateIfNull(node.text())
                Title() -> channelFactory.articleBuilder.title(
                    node.wholeText().trim()
                )

                Entry.Author.Author() -> {
                    val name = node.children()
                        .firstOrNull { it.tagName() == Entry.Author.Name() }

                    if (name != null) {
                        channelFactory.articleBuilder.author(name.text().trim())
                    }
                }

                Entry.Guid() -> channelFactory.articleBuilder.guid(node.text())
                Entry.Content() -> {
                    val type = node.attr("type")

                    val text = when {
                        type == "xhtml" -> node.html()
                        else -> node.data().ifBlank { node.wholeText() }
                    }

                    channelFactory.articleBuilder.content(text.trim())
                    channelFactory.setImageFromContent(node)
                }

                Entry.Description() -> {
                    channelFactory.articleBuilder.description(node.wholeText().trim())
                    channelFactory.setImageFromContent(node)
                }

                Entry.Category() -> {
                    val text = node.attr(Entry.Term())
                    if (text.isNotEmpty()) {
                        channelFactory.articleBuilder.addCategory(text)
                    }
                }

                Entry.YouTubeVideoID() -> channelFactory.articleBuilder.youtubeVideoID(node.text())

                Entry.Media.Group() -> media(node)
            }
        }

        channelFactory.buildArticle()
    }

    private fun media(mediaGroup: Element) {
        mediaGroup.children().forEach { node ->
            when (node.tagName()) {
                Entry.Media.Title() -> channelFactory.articleMediaBuilder.title(node.text())
                Entry.Media.Content() -> channelFactory.articleMediaBuilder.contentUrl(
                    node.attr("url").ifBlank { null })

                Entry.Media.Thumbnail() -> channelFactory.articleMediaBuilder.thumbnailUrl(
                    node.attr(
                        "url"
                    ).ifBlank { null })

                Entry.Media.Description() -> channelFactory.articleMediaBuilder.description(node.wholeText())
            }
        }
    }

    private fun withLink(element: Element, callback: (href: String) -> Unit) {
        val href = element.attr(Link.Href())
        val rel = element.attr(Link.Rel())

        if (!filteredRelations.contains(rel)) {
            callback(href)
        }
    }

    companion object {
        val filteredRelations = listOf(
            Link.Edit(),
            Link.Self(),
            Link.Replies(),
            Link.Enclosure(),
        )
    }
}
