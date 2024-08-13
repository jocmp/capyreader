package com.prof18.rssparser.internal

internal sealed class RssKeyword(val value: String) {
    data object Rss: RssKeyword("rss")
    data object Title : RssKeyword("title")
    data object Image : RssKeyword("image")
    data object Link : RssKeyword("link")
    data object Href : RssKeyword("href")
    data object Url : RssKeyword("url")
    data object Description : RssKeyword("description")

    object Itunes {
        data object Author : RssKeyword("itunes:author")
        data object Duration : RssKeyword("itunes:duration")
        data object Keywords : RssKeyword("itunes:keywords")
        data object Image : RssKeyword("itunes:image")
        data object Explicit : RssKeyword("itunes:explicit")
        data object Subtitle : RssKeyword("itunes:subtitle")
        data object Summary : RssKeyword("itunes:summary")
    }

    object Channel {
        data object Channel : RssKeyword("channel")
        data object UpdatePeriod : RssKeyword("sy:updatePeriod")
        data object LastBuildDate : RssKeyword("lastBuildDate")

        object Itunes {
            data object Category : RssKeyword("itunes:category")
            data object Owner : RssKeyword("itunes:owner")
            data object OwnerName : RssKeyword("itunes:name")
            data object OwnerEmail : RssKeyword("itunes:email")
            data object Type : RssKeyword("itunes:type")
            data object NewFeedUrl : RssKeyword("itunes:new-feed-url")
            data object Text : RssKeyword("text")
        }
    }

    object Item {
        data object Item : RssKeyword("item")
        data object Author : RssKeyword("dc:creator")
        data object Category : RssKeyword("category")
        data object Thumbnail : RssKeyword("media:thumbnail")
        data object MediaContent : RssKeyword("media:content")
        data object Enclosure : RssKeyword("enclosure")
        data object Content : RssKeyword("content:encoded")
        data object PubDate : RssKeyword("pubDate")
        data object Time : RssKeyword("time")
        data object Type : RssKeyword("type")
        data object Guid : RssKeyword("guid")
        data object Source : RssKeyword("source")
        data object Comments : RssKeyword("comments")
        data object Thumb: RssKeyword("thumb")

        object News {
            data object Image : RssKeyword("News:Image")
        }

        object Itunes {
            data object Episode : RssKeyword("itunes:episode")
            data object Season : RssKeyword("itunes:season")
            data object EpisodeType : RssKeyword("itunes:episodeType")
        }
    }
}
