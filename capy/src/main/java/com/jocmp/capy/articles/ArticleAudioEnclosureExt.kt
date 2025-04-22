package com.jocmp.capy.articles

import com.jocmp.capy.Article

val Article.audioEnclosure: String?
    get() {
//        return enclosures.firstOrNull()?.let {
        return """
            <div class="audio-enclosure">
                <div class="audio-enclosure__details">
                    <img class="audio-enclosure__image" src="https://megaphone.imgix.net/podcasts/e4c412e6-e1f0-11e8-9bde-83f9d376f059/image/Podcast_Tile_6000x6000px.png?ixlib=rails-4.3.1&max-w=3000&max-h=3000&fit=crop&auto=format,compress">
                    <div class="audio-enclosure__details-text">
                        <div class="audio-enclosure__feed-title">$feedName</div>
                        <div>$title</div>
                        <div>115 minutes left</div>
                    </div>
                </div>
                <div class="audio-enclosure__actions">
                    <div>▶️</div>
                </div>
            </div>
            """.trimIndent()
    }
