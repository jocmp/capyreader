package com.capyreader.app.common

import android.content.Context
import coil.imageLoader
import coil.request.ImageRequest
import com.jocmp.capy.accounts.FaviconFetcher

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
}
