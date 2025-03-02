package com.capyreader.app.common

import android.content.Context
import coil.imageLoader
import coil.request.ImageRequest
import com.jocmp.capy.Feed
import com.jocmp.capy.accounts.FaviconFetcher
import java.net.MalformedURLException
import java.net.URI

class AppFaviconFetcher(private val context: Context) : FaviconFetcher {
    override suspend fun isValid(url: String?): Boolean {
        url ?: return false

        val result = context.imageLoader
            .execute(
                ImageRequest.Builder(context)
                    .data(url)
                    .build()
            )

        return result.drawable != null
    }

    override suspend fun findFaviconURL(feed: Feed): String? {
        val baseURL = feed.siteURL.ifBlank { feed.feedURL }

        return try {
            val faviconURL = URI(baseURL).resolve("favicon.ico").toString()

            if (isValid(faviconURL)) {
                return faviconURL
            }

            return null
        } catch (e: MalformedURLException) {
            null
        }
    }
}
