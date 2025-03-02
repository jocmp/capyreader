package com.jocmp.capy.accounts.local

import com.jocmp.rssparser.model.RssItem

internal object RichMedia {
    fun parse(item: RssItem): String? {
        val media = item.media ?: return null
        val videoID = item.youtubeVideoID ?: return null
        val description = media.description ?: return null

        return """
           <div>
             <iframe src="https://www.youtube.com/embed/$videoID" />
             <p>$description</p>
           </div>
        """.trimIndent()
    }
}
