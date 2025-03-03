package com.jocmp.capy.accounts

interface FaviconFetcher {
    suspend fun isValid(url: String?): Boolean
}
