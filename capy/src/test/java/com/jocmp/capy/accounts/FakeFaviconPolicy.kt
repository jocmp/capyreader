package com.jocmp.capy.accounts

object FakeFaviconPolicy : FaviconPolicy {
    override suspend fun isValid(url: String?) = true
}
