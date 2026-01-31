/*
*   Copyright 2016 Marco Gomiero
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
*/

package com.jocmp.rssparser.model

import com.jocmp.rssparser.internal.ImagePolicy
import com.jocmp.rssparser.internal.decodeNumericEntities

data class RssItem(
    val guid: String?,
    val title: String?,
    val author: String?,
    val link: String?,
    val pubDate: String?,
    val description: String?,
    val content: String?,
    val image: String?,
    val audio: String?,
    val video: String?,
    val sourceName: String?,
    val sourceUrl: String?,
    val categories: List<String>,
    val itunesItemData: ItunesItemData?,
    val media: Media?,
    val youtubeVideoID: String?,
    val commentsUrl: String?,
    val enclosures: List<RssItemEnclosure>
) {
    data class Builder(
        private var guid: String? = null,
        private var title: String? = null,
        private var author: String? = null,
        private var link: String? = null,
        private var pubDate: String? = null,
        private var description: String? = null,
        private var content: String? = null,
        private var image: String? = null,
        private var audio: String? = null,
        private var video: String? = null,
        private var sourceName: String? = null,
        private var sourceUrl: String? = null,
        private val categories: MutableList<String> = mutableListOf(),
        private var itunesItemData: ItunesItemData? = null,
        private var media: Media? = null,
        private var youtubeVideoID: String? = null,
        private var enclosures: MutableList<RssItemEnclosure> = mutableListOf(),
        private var commentUrl: String? = null,
    ) {
        fun guid(guid: String?) = apply { this.guid = guid }
        fun title(title: String?) = apply {
            this.title = title?.let { decodeNumericEntities(it) }
        }
        fun author(author: String?) = apply { this.author = author }
        fun link(link: String?) = apply { this.link = link }
        fun currentLink(): String? = link
        fun pubDate(pubDate: String?) = apply {
            this.pubDate = pubDate
        }

        fun pubDateIfNull(pubDate: String?) = apply {
            if (this.pubDate == null) {
                this.pubDate = pubDate
            }
        }

        fun description(description: String?) = apply { this.description = description }
        fun content(content: String?) = apply { this.content = content }
        fun image(image: String?) = apply {
            if (this.image == null && image != null && ImagePolicy.isValidArticleImage(image)) {
                this.image = image
            }
        }

        fun audio(audio: String?) = apply { this.audio = audio }
        fun audioIfNull(audio: String?) = apply {
            if (this.audio == null) {
                this.audio = audio
            }
        }

        fun video(video: String?) = apply { this.video = video }
        fun videoIfNull(video: String?) = apply {
            if (this.video == null) {
                this.video = video
            }
        }

        fun sourceName(sourceName: String?) = apply { this.sourceName = sourceName }
        fun sourceUrl(sourceUrl: String?) = apply { this.sourceUrl = sourceUrl }

        fun addCategory(category: String?) = apply {
            if (category != null) {
                this.categories.add(category)
            }
        }

        fun addEnclosure(url: String, type: String) = apply {
            if (url.isNotBlank() && type.isNotBlank()) {
                this.enclosures.add(RssItemEnclosure(url = url, type = type))
            }
        }

        fun itunesArticleData(itunesItemData: ItunesItemData?) =
            apply { this.itunesItemData = itunesItemData }

        fun media(media: Media?) = apply { this.media = media }

        fun commentUrl(url: String?) = apply { this.commentUrl = url }

        fun youtubeVideoID(id: String?) = apply { this.youtubeVideoID = id }

        fun build() = RssItem(
            guid = guid,
            title = title,
            author = author,
            link = link,
            pubDate = pubDate,
            description = description,
            content = content,
            image = image,
            audio = audio,
            video = video,
            sourceName = sourceName,
            sourceUrl = sourceUrl,
            categories = categories,
            itunesItemData = itunesItemData,
            commentsUrl = commentUrl,
            media = media,
            enclosures = enclosures,
            youtubeVideoID = youtubeVideoID,
        )
    }
}
