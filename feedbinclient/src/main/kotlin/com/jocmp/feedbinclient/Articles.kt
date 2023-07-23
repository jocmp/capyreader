package com.jocmp.feedbinclient

import com.jocmp.feedbinclient.api.FeedbinClient
import com.jocmp.feedbinclient.api.PagingInfo
import com.jocmp.feedbinclient.common.request
import com.jocmp.feedbinclient.db.FeedbinDatabase
import okhttp3.Headers

interface Articles {
    suspend fun all(): Result<List<Article>>
}



class DefaultArticles(
    private val database: FeedbinDatabase,
    private val client: FeedbinClient,
) : Articles {
    override suspend fun all(): Result<List<Article>> {
        val firstResult = request { client.entries() }.getOrElse {
            return Result.failure(it)
        }
        var pagingInfo = PagingInfo.fromHeader(firstResult.headers()["links"])

        // save articles
        // recurse


        return Result.success(emptyList())
    }

    private fun fetchEntries(page: String) {
        // if page is null, return
        // save articles
        // recurse
    }
}
