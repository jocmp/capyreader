package com.jocmp.feedbinclient

import okhttp3.Headers
import retrofit2.Response
import java.net.URL

data class PagingInfo(
    val nextPage: Int? = null,
    val lastPage: Int? = null
) {
    companion object {
        private val trimmedTokens = listOf(' ', '<', '>')

        internal fun fromHeader(linkHeader: String?): PagingInfo? {
            linkHeader ?: return null

            val links = linkHeader.split(",")
            val rels = mutableMapOf<String, String?>()

            links.forEach { link ->
                val parts = link.split("; ")
                val url = parts[0].trim { trimmedTokens.contains(it) }
                rels[parts[1]] = query(url)["page"]
            }

            return PagingInfo(
                nextPage = optionalRel(rels, "next"),
                lastPage = optionalRel(rels, "last")
            )
        }

        private fun query(url: String): Map<String, String> {
            return URL(url).query
                .split("&")
                .associate { pair ->
                    val (key, value) = pair.split("=")

                    key to value
                }
        }

        private fun optionalRel(rels: Map<String, String?>, key: String): Int? {
            return rels["rel=\"${key}\""]?.toIntOrNull()
        }
    }
}

val <T> Response<T>.pagingInfo: PagingInfo?
    get() = PagingInfo.fromHeader(headers().get("links"))
