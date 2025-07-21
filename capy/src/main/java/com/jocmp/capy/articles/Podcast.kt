package com.jocmp.capy.articles

import com.jocmp.capy.Enclosure

data class Podcast(
    val articleID: String,
    val title: String,
    val feedName: String,
    val enclosure: Enclosure
)