package com.jocmp.rssparser.exception

data class NonFeedResponseException(
    val url: String,
    val detectedType: String,
) : Exception("Response at $url is not a feed. Detected $detectedType")
