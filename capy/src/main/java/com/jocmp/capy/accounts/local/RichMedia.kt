package com.jocmp.capy.accounts.local

import com.jocmp.rssparser.model.RssItem

internal class RichMedia {
    companion object {
        fun parse(item: RssItem): String? {
            if (item.image != null) {
                val description = item.content?.ifBlank { item.description }.orEmpty()

                return """
                    <div>
                    $description
                    <img src="${item.image}" />
                    </div>
                """.trimIndent()
            }

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
}
