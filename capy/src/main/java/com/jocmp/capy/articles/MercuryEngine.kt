package com.jocmp.capy.articles

fun interface MercuryEngine {
    suspend fun parse(url: String, html: String): Result

    data class Result(
        val content: String?,
        val leadImageUrl: String?,
        val error: Boolean = false,
        val message: String? = null,
    )
}
