package com.jocmp.rssparser.internal

import com.jocmp.rssparser.model.ItunesChannelData
import com.jocmp.rssparser.model.ItunesItemData
import com.jocmp.rssparser.model.ItunesOwner
import com.jocmp.rssparser.model.Media
import com.jocmp.rssparser.model.RssChannel
import com.jocmp.rssparser.model.RssImage
import com.jocmp.rssparser.model.RssItem
import org.jsoup.Jsoup
import org.jsoup.nodes.Element


internal class ChannelFactory {
    val channelBuilder = RssChannel.Builder()
    val channelImageBuilder = RssImage.Builder()
    var articleBuilder = RssItem.Builder()
    val itunesChannelBuilder = ItunesChannelData.Builder()
    var itunesArticleBuilder = ItunesItemData.Builder()
    var itunesOwnerBuilder = ItunesOwner.Builder()
    var articleMediaBuilder = Media.Builder()

    // This image url is extracted from the content and the description of the rss item.
    // It's a fallback just in case there aren't any images in the enclosure tag.
    private var imageUrlFromContent: String? = null

    fun buildArticle() {
        articleBuilder.image(imageUrlFromContent)
        articleBuilder.itunesArticleData(itunesArticleBuilder.build())
        articleBuilder.media(articleMediaBuilder.build())
        channelBuilder.addItem(articleBuilder.build())
        // Reset temp data
        imageUrlFromContent = null
        articleBuilder = RssItem.Builder()
        itunesArticleBuilder = ItunesItemData.Builder()
        articleMediaBuilder = Media.Builder()
    }

    fun buildItunesOwner() {
        itunesChannelBuilder.owner(itunesOwnerBuilder.build())
        itunesOwnerBuilder = ItunesOwner.Builder()
    }

    /**
     * Finds the first img tag and gets the src as the featured image.
     *
     * @param content The content in which to search for the tag
     * @return The url, if there is one
     */
    fun setImageFromContent(content: Element) {
        val image = Jsoup.parse(content.wholeText()).selectFirst("img") ?: return
        val imageSource = image.attr("src")

        if (ImagePolicy.isValidArticleImage(imageSource)) {
            imageUrlFromContent = imageSource
        }
    }

    fun setChannelItunesKeywords(keywords: String?) {
        val keywordList = extractItunesKeywords(keywords)
        if (keywordList.isNotEmpty()) {
            itunesChannelBuilder.keywords(keywordList)
        }
    }

    fun setArticleItunesKeywords(keywords: String?) {
        val keywordList = extractItunesKeywords(keywords)
        if (keywordList.isNotEmpty()) {
            itunesArticleBuilder.keywords(keywordList)
        }
    }

    private fun extractItunesKeywords(keywords: String?): List<String> =
        keywords?.split(",")?.mapNotNull {
            it.ifEmpty {
                null
            }
        } ?: emptyList()


    fun build(): RssChannel {
        val channelImage = channelImageBuilder.build()
        if (channelImage.isNotEmpty()) {
            channelBuilder.image(channelImage)
        }
        channelBuilder.itunesChannelData(itunesChannelBuilder.build())
        return channelBuilder.build()
    }
}
