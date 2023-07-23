package com.jocmp.feedbinclient.api

import android.net.Uri

data class PagingInfo(
    val nextPage: String? = null,
    val lastPage: String? = null
) {
    companion object {
        private val trimmedTokens = listOf(' ', '<', '>')

        fun fromHeader(linkHeader: String?): PagingInfo {
            linkHeader ?: return PagingInfo()

            val links = linkHeader.split(",")
            val rels = mutableMapOf<String, String?>()

            links.forEach { link ->
                val parts = link.split("; ")
                val url = parts[0].trim { trimmedTokens.contains(it) }
                rels[parts[1]] = Uri.parse(url).getQueryParameter("page")
            }

            return PagingInfo(nextPage = rels["rel=\"next\""], lastPage = rels["rel=\"last\""])
        }
    }
}
