package com.jocmp.rssparser.internal.rss

import com.jocmp.rssparser.internal.ChannelFactory
import com.jocmp.rssparser.internal.FeedHandler
import com.jocmp.rssparser.model.RssChannel
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

internal class RssFeedHandler(val document: Document) : FeedHandler {
    private var channelFactory = ChannelFactory()

    override fun build(): RssChannel {
        channel()

        return channelFactory.build()
    }

    private fun channel() {
        withDocument(RssKeyword.Channel.Channel.value) { node ->
            when (node.tagName()) {
                RssKeyword.Title.value -> channelFactory.channelBuilder.title(node.text())
                RssKeyword.Link.value -> channelFactory.channelBuilder.link(node.text())
                RssKeyword.Image.value -> channelImage(node)
                RssKeyword.Description.value -> {
                    channelFactory.setImageFromContent(node)
                    channelFactory.channelBuilder.description(node.text())
                }

                RssKeyword.Channel.LastBuildDate.value -> {
                    channelFactory.channelBuilder.lastBuildDate(node.text())
                }

                RssKeyword.Channel.UpdatePeriod.value -> {
                    channelFactory.channelBuilder.updatePeriod(node.text())
                }

                RssKeyword.Channel.Itunes.Type.value -> {
                    channelFactory.itunesChannelBuilder.type(node.text())
                }

                RssKeyword.Itunes.Image.value -> {
                    val url = node.attr(RssKeyword.Href.value)
                    channelFactory.itunesChannelBuilder.image(url)
                }


                RssKeyword.Itunes.Explicit.value -> {
                    channelFactory.itunesChannelBuilder.explicit(node.text())
                }

                RssKeyword.Itunes.Subtitle.value -> {
                    channelFactory.itunesChannelBuilder.subtitle(node.text())
                }

                RssKeyword.Itunes.Summary.value -> {
                    channelFactory.itunesChannelBuilder.summary(node.wholeText().trim())
                }

                RssKeyword.Itunes.Author.value -> {
                    channelFactory.itunesChannelBuilder.author(node.text())
                }

                RssKeyword.Itunes.Duration.value -> {
                    channelFactory.itunesChannelBuilder.duration(node.text())
                }

                RssKeyword.Itunes.Keywords.value -> {
                    channelFactory.setChannelItunesKeywords(node.text())
                }

                RssKeyword.Channel.Itunes.Category.value -> {
                    val category = node.attr(RssKeyword.Channel.Itunes.Text.value)
                    channelFactory.itunesChannelBuilder.addCategory(category)

                    node.children().forEach { child ->
                        val childCategory = child.attr(RssKeyword.Channel.Itunes.Text.value)
                        channelFactory.itunesChannelBuilder.addCategory(childCategory)
                    }
                }

                RssKeyword.Channel.Itunes.NewFeedUrl.value -> {
                    channelFactory.itunesChannelBuilder.newsFeedUrl(node.text())
                }

                RssKeyword.Channel.Itunes.Owner.value -> itunesOwner(node)

                RssKeyword.Item.Item.value -> item(node)
            }
        }
    }

    private fun itunesOwner(itunesOwner: Element) {
        itunesOwner.children().forEach { node ->
            when (node.tagName()) {
                RssKeyword.Channel.Itunes.OwnerName.value -> {
                    channelFactory.itunesOwnerBuilder.name(node.text())
                }

                RssKeyword.Channel.Itunes.OwnerEmail.value -> {
                    channelFactory.itunesOwnerBuilder.email(node.text())
                }
            }
        }

        channelFactory.buildItunesOwner()
    }

    private fun channelImage(channelImage: Element) {
        channelImage.children().forEach { node ->

            when (node.tagName()) {
                RssKeyword.Url.value -> channelFactory.channelImageBuilder.url(node.text())
                RssKeyword.Title.value -> channelFactory.channelImageBuilder.title(node.text())
                RssKeyword.Link.value -> channelFactory.channelImageBuilder.link(node.text())
                RssKeyword.Description.value -> {
                    channelFactory.channelImageBuilder.description(node.text())
                }
            }
        }
    }

    private fun item(item: Element) {
        item.children().forEach { node ->
            when (node.tagName()) {
                RssKeyword.Item.Author.value -> channelFactory.articleBuilder.author(node.text())
                RssKeyword.Item.DCAuthor.value -> channelFactory.articleBuilder.author(node.text())
                RssKeyword.Item.Category.value -> channelFactory.articleBuilder.addCategory(node.text())
                RssKeyword.Item.Source.value -> {
                    channelFactory.articleBuilder.sourceName(node.text())
                    val sourceUrl = node.attr(RssKeyword.Url.value)
                    channelFactory.articleBuilder.sourceUrl(sourceUrl)
                }

                RssKeyword.Item.Time.value,
                RssKeyword.Item.DCDate.value -> {
                    channelFactory.articleBuilder.pubDate(node.text())
                }

                RssKeyword.Item.Guid.value -> channelFactory.articleBuilder.guid(node.text())
                RssKeyword.Item.Content.value -> {
                    channelFactory.articleBuilder.content(node.text())
                    channelFactory.setImageFromContent(node)
                }

                RssKeyword.Item.PubDate.value -> {
                    channelFactory.articleBuilder.pubDate(node.text())
                }

                RssKeyword.Item.News.Image.value -> channelFactory.articleBuilder.image(node.text())
                RssKeyword.Item.MediaContent.value -> {
                    val url = node.attr(RssKeyword.Url.value)
                    channelFactory.articleBuilder.image(url)

                    val type = node.attr(RssKeyword.Item.Type.value)

                    channelFactory.articleBuilder.addEnclosure(url = url, type = type)
                }

                RssKeyword.Item.Thumbnail.value -> {
                    val url = node.attr(RssKeyword.Url.value)
                    channelFactory.articleBuilder.image(url)
                }

                RssKeyword.Item.Thumb.value -> {
                    channelFactory.articleBuilder.image(node.text())
                }

                RssKeyword.Image.value -> {
                    channelFactory.articleBuilder.image(node.text())
                }

                RssKeyword.Item.Enclosure.value -> {
                    val type = node.attr(RssKeyword.Item.Type.value)
                    when {
                        type.contains("image") -> {
                            channelFactory.articleBuilder.image(node.attr(RssKeyword.Url.value))
                        }

                        type.contains("audio") -> {
                            channelFactory.articleBuilder.audioIfNull(node.attr(RssKeyword.Url.value))
                        }

                        type.contains("video") -> {
                            channelFactory.articleBuilder.videoIfNull(node.attr(RssKeyword.Url.value))
                        }

                        type.isNullOrBlank() -> {
                            channelFactory.articleBuilder.image(node.text())
                        }
                    }
                }

                RssKeyword.Item.Comments.value -> channelFactory.articleBuilder.commentUrl(node.text())
                RssKeyword.Link.value -> channelFactory.articleBuilder.link(node.text())
                RssKeyword.Description.value -> {
                    val wholeText = node.wholeText()

                    channelFactory.setImageFromContent(node)

                    channelFactory.articleBuilder.description(wholeText.trim())
                }

                RssKeyword.Title.value -> channelFactory.articleBuilder.title(
                    node.wholeText().trim()
                )

                RssKeyword.Itunes.Keywords.value -> {
                    channelFactory.setArticleItunesKeywords(node.text())
                }

                RssKeyword.Item.Itunes.Season.value -> {
                    channelFactory.itunesArticleBuilder.season(node.text())
                }

                RssKeyword.Item.Itunes.Episode.value -> {
                    channelFactory.itunesArticleBuilder.episode(node.text())
                }

                RssKeyword.Itunes.Image.value -> {
                    val url = node.attr(RssKeyword.Href.value)
                    channelFactory.itunesArticleBuilder.image(url)
                }

                RssKeyword.Itunes.Author.value -> {
                    channelFactory.itunesArticleBuilder.author(node.text())
                }

                RssKeyword.Item.Itunes.EpisodeType.value -> {
                    channelFactory.itunesArticleBuilder.episodeType(node.text())
                }


                RssKeyword.Itunes.Summary.value -> {
                    channelFactory.itunesArticleBuilder.summary(node.wholeText().trim())
                }

                RssKeyword.Itunes.Duration.value -> {
                    channelFactory.itunesArticleBuilder.duration(node.text())
                }

                RssKeyword.Itunes.Explicit.value -> {
                    channelFactory.itunesArticleBuilder.explicit(node.text())
                }

                RssKeyword.Itunes.Subtitle.value -> {
                    channelFactory.itunesArticleBuilder.subtitle(node.text())
                }
            }
        }

        channelFactory.buildArticle()
    }

    private fun withDocument(selector: String, onElement: (element: Element) -> Unit) {
        document.select(selector).first()?.children()?.forEach(onElement)
    }
}
