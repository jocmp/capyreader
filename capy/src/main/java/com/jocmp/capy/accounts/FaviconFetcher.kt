package com.jocmp.capy.accounts

import com.jocmp.capy.Feed

interface FaviconFetcher {
    suspend fun isValid(url: String?): Boolean

    suspend fun findFaviconURL(feed: Feed): String?
}
