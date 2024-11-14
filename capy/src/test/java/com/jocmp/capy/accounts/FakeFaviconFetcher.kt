package com.jocmp.capy.accounts

object FakeFaviconFetcher : FaviconFetcher {
    override suspend fun isValid(url: String?) = true
}
