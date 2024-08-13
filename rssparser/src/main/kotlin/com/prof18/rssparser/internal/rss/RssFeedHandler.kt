package com.prof18.rssparser.internal.rss

import com.prof18.rssparser.internal.ChannelFactory
import com.prof18.rssparser.internal.FeedHandler
import com.prof18.rssparser.internal.RssKeyword
import com.prof18.rssparser.model.RssChannel
import org.xml.sax.Attributes

internal class RssFeedHandler : FeedHandler {

    private var isInsideItem = false
    private var isInsideChannel = true
    private var isInsideChannelImage = false
    private var isInsideItunesOwner = false
    private var isInsideItemImage = false

    private var channelFactory = ChannelFactory()

    override fun onStartRssElement(
        qName: String?,
        attributes: Attributes?,
    ) {
        when (qName) {
            RssKeyword.Channel.Channel.value -> isInsideChannel = true
            RssKeyword.Item.Item.value -> isInsideItem = true
            RssKeyword.Channel.Itunes.Owner.value -> isInsideItunesOwner = true
            RssKeyword.Image.value -> {
                when {
                    isInsideItem -> {
                        isInsideItemImage = true
                    }
                    isInsideChannel -> isInsideChannelImage = true
                }
            }

            RssKeyword.Item.MediaContent.value -> {
                if (isInsideItem) {
                    val url = attributes?.getValue(RssKeyword.Url.value)
                    channelFactory.articleBuilder.image(url)
                }
            }

            RssKeyword.Item.Thumbnail.value -> {
                if (isInsideItem) {
                    val url = attributes?.getValue(RssKeyword.Url.value)
                    channelFactory.articleBuilder.image(url)
                }
            }

            RssKeyword.Item.Enclosure.value -> {
                if (isInsideItem) {
                    val type = attributes?.getValue(RssKeyword.Item.Type.value)

                    when {
                        type != null && type.contains("image") -> {
                            // If there are multiple elements, we take only the first
                            channelFactory.articleBuilder.image(
                                attributes.getValue(RssKeyword.Url.value)
                            )
                        }

                        type != null && type.contains("audio") -> {
                            // If there are multiple elements, we take only the first
                            channelFactory.articleBuilder.audioIfNull(
                                attributes.getValue(RssKeyword.Url.value)
                            )
                        }

                        type != null && type.contains("video") -> {
                            // If there are multiple elements, we take only the first
                            channelFactory.articleBuilder.videoIfNull(
                                attributes.getValue(RssKeyword.Url.value)
                            )
                        }
                    }
                }
            }

            RssKeyword.Itunes.Image.value -> {
                val url = attributes?.getValue(RssKeyword.Href.value)
                when {
                    isInsideItem -> channelFactory.itunesArticleBuilder.image(url)
                    isInsideChannel -> channelFactory.itunesChannelBuilder.image(url)
                }
            }

            RssKeyword.Item.Source.value -> {
                if (isInsideItem) {
                    val sourceUrl = attributes?.getValue(RssKeyword.Url.value)
                    channelFactory.articleBuilder.sourceUrl(sourceUrl)
                }
            }

            RssKeyword.Channel.Itunes.Category.value -> {
                if (isInsideChannel) {
                    val category = attributes?.getValue(RssKeyword.Channel.Itunes.Text.value)
                    channelFactory.itunesChannelBuilder.addCategory(category)
                }
            }
        }
    }

    override fun endElement(
        qName: String?,
        text: String,
    ) {
        when {
            isInsideItemImage -> {
                when (qName) {
                    RssKeyword.Link.value -> {
                        channelFactory.articleBuilder.image(text)
                    }
                    RssKeyword.Image.value -> {
                        channelFactory.articleBuilder.image(text)
                        isInsideItemImage = false
                    }
                }
            }

            isInsideItem -> {
                when (qName) {
                    RssKeyword.Item.Author.value -> channelFactory.articleBuilder.author(text)
                    RssKeyword.Item.Category.value -> channelFactory.articleBuilder.addCategory(text)
                    RssKeyword.Item.Source.value -> channelFactory.articleBuilder.sourceName(text)
                    RssKeyword.Item.Time.value -> channelFactory.articleBuilder.pubDate(text)
                    RssKeyword.Item.Guid.value -> channelFactory.articleBuilder.guid(text)
                    RssKeyword.Item.Content.value -> {
                        channelFactory.articleBuilder.content(text)
                        channelFactory.setImageFromContent(text)
                    }

                    RssKeyword.Item.PubDate.value -> {
                        channelFactory.articleBuilder.pubDate(text)
                    }

                    RssKeyword.Item.News.Image.value -> channelFactory.articleBuilder.image(text)
                    RssKeyword.Item.Comments.value -> channelFactory.articleBuilder.commentUrl(text)
                    RssKeyword.Image.value -> channelFactory.articleBuilder.image(text)
                    RssKeyword.Title.value -> channelFactory.articleBuilder.title(text)
                    RssKeyword.Link.value -> {
                        if (!isInsideItemImage) {
                            channelFactory.articleBuilder.link(text)
                        }
                    }
                    RssKeyword.Description.value -> {
                        channelFactory.setImageFromContent(text)
                        channelFactory.articleBuilder.description(text)
                    }

                    RssKeyword.Item.Enclosure.value -> {
                        channelFactory.articleBuilder.image(text)
                    }

                    RssKeyword.Item.Thumb.value -> {
                        channelFactory.articleBuilder.image(text)
                    }

                    RssKeyword.Item.Itunes.EpisodeType.value -> {
                        channelFactory.itunesArticleBuilder.episodeType(text)
                    }

                    RssKeyword.Item.Itunes.Episode.value -> {
                        channelFactory.itunesArticleBuilder.episode(text)
                    }

                    RssKeyword.Item.Itunes.Season.value -> {
                        channelFactory.itunesArticleBuilder.season(text)
                    }

                    RssKeyword.Itunes.Explicit.value -> {
                        channelFactory.itunesArticleBuilder.explicit(text)
                    }

                    RssKeyword.Itunes.Subtitle.value -> {
                        channelFactory.itunesArticleBuilder.subtitle(text)
                    }

                    RssKeyword.Itunes.Summary.value -> {
                        channelFactory.itunesArticleBuilder.summary(text)
                    }

                    RssKeyword.Itunes.Author.value -> {
                        channelFactory.itunesArticleBuilder.author(text)
                    }

                    RssKeyword.Itunes.Duration.value -> {
                        channelFactory.itunesArticleBuilder.duration(text)
                    }

                    RssKeyword.Itunes.Keywords.value -> channelFactory.setArticleItunesKeywords(text)

                    RssKeyword.Item.Item.value -> {
                        channelFactory.buildArticle()
                        isInsideItem = false
                    }
                }
            }

            isInsideItunesOwner -> {
                when (qName) {
                    RssKeyword.Channel.Itunes.OwnerName.value -> {
                        channelFactory.itunesOwnerBuilder.name(text)
                    }

                    RssKeyword.Channel.Itunes.OwnerEmail.value -> {
                        channelFactory.itunesOwnerBuilder.email(text)
                    }

                    RssKeyword.Channel.Itunes.Owner.value -> {
                        channelFactory.buildItunesOwner()
                        isInsideItunesOwner = false
                    }
                }
            }

            isInsideChannelImage -> {
                when (qName) {
                    RssKeyword.Url.value -> channelFactory.channelImageBuilder.url(text)
                    RssKeyword.Title.value -> channelFactory.channelImageBuilder.title(text)
                    RssKeyword.Link.value -> channelFactory.channelImageBuilder.link(text)
                    RssKeyword.Description.value -> {
                        channelFactory.channelImageBuilder.description(text)
                    }

                    RssKeyword.Image.value -> isInsideChannelImage = false
                }
            }

            isInsideChannel -> {
                when (qName) {
                    RssKeyword.Title.value -> channelFactory.channelBuilder.title(text)
                    RssKeyword.Link.value -> channelFactory.channelBuilder.link(text)
                    RssKeyword.Description.value -> channelFactory.channelBuilder.description(text)
                    RssKeyword.Channel.LastBuildDate.value -> {
                        channelFactory.channelBuilder.lastBuildDate(text)
                    }

                    RssKeyword.Channel.UpdatePeriod.value -> {
                        channelFactory.channelBuilder.updatePeriod(text)
                    }

                    RssKeyword.Channel.Itunes.Type.value -> {
                        channelFactory.itunesChannelBuilder.type(text)
                    }

                    RssKeyword.Itunes.Explicit.value -> {
                        channelFactory.itunesChannelBuilder.explicit(text)
                    }

                    RssKeyword.Itunes.Subtitle.value -> {
                        channelFactory.itunesChannelBuilder.subtitle(text)
                    }

                    RssKeyword.Itunes.Summary.value -> {
                        channelFactory.itunesChannelBuilder.summary(text)
                    }

                    RssKeyword.Itunes.Author.value -> {
                        channelFactory.itunesChannelBuilder.author(text)
                    }

                    RssKeyword.Itunes.Duration.value -> {
                        channelFactory.itunesChannelBuilder.duration(text)
                    }

                    RssKeyword.Itunes.Keywords.value -> {
                        channelFactory.setChannelItunesKeywords(text)
                    }

                    RssKeyword.Channel.Itunes.NewFeedUrl.value -> {
                        channelFactory.itunesChannelBuilder.newsFeedUrl(text)
                    }

                    RssKeyword.Channel.Channel.value -> isInsideChannel = false
                }
            }
        }
    }

    override fun buildRssChannel(): RssChannel =
        channelFactory.build()

}
