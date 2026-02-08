package com.capyreader.app.common

import android.content.Context
import coil3.imageLoader
import coil3.request.ImageRequest
import com.jocmp.capy.accounts.FaviconPolicy

class AppFaviconPolicy(private val context: Context) : FaviconPolicy {
    override suspend fun isValid(url: String?): Boolean {
        url ?: return false

        val result = context.imageLoader
            .execute(
                ImageRequest.Builder(context)
                    .data(url)
                    .build()
            )

        return result.image != null
    }
}
