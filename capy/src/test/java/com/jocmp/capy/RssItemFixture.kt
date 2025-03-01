package com.jocmp.capy

import com.jocmp.rssparser.model.ItunesItemData
import com.jocmp.rssparser.model.Media
import com.jocmp.rssparser.model.RssItem

fun rssItemFixture(
    guid: String? = null,
    title: String? = null,
    author: String? = null,
    link: String? = null,
    pubDate: String? = null,
    description: String? = null,
    content: String? = null,
    image: String? = null,
    audio: String? = null,
    video: String? = null,
    sourceName: String? = null,
    sourceUrl: String? = null,
    categories: MutableList<String> = mutableListOf(),
    itunesItemData: ItunesItemData? = null,
    youtubeVideoID: String? = null,
    media: Media? = null,
    commentUrl: String? = null,
) = RssItem(
    guid,
    title,
    author,
    link,
    pubDate,
    description,
    content,
    image,
    audio,
    video,
    sourceName,
    sourceUrl,
    categories,
    itunesItemData,
    media,
    youtubeVideoID,
    commentUrl,
)
