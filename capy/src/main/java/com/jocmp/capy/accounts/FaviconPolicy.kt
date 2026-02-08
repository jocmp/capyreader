package com.jocmp.capy.accounts

fun interface FaviconPolicy {
    suspend fun isValid(url: String?): Boolean
}
