package com.jocmp.capy.articles

fun interface ContentExtractor {
    suspend fun extract(url: String?, html: String): Result<String>
}
