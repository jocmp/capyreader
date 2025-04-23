package com.jocmp.capy

import java.net.URL

data class Enclosure(
    val url: URL,
    val type: String,
    val itunesDurationSeconds: Long?,
    val itunesImage: String?
)
