package com.jocmp.rssparser.model

data class ConditionalGetInfo(
    val etag: String? = null,
    val lastModified: String? = null,
) {
    val isEmpty: Boolean
        get() = etag.isNullOrBlank() && lastModified.isNullOrBlank()

    companion object {
        val EMPTY = ConditionalGetInfo()
    }
}

data class RssChannelResult(
    val channel: RssChannel?,
    val conditionalGet: ConditionalGetInfo,
)
