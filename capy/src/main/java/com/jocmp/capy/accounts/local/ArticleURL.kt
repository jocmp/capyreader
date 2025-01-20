package com.jocmp.capy.accounts.local

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.net.MalformedURLException
import java.net.URL

internal object ArticleURL {
    internal fun parse(url: URL): URL {
        return googleAlertURL(url) ?: url
    }

    private fun googleAlertURL(url: URL): URL? {
        if (url.host != GOOGLE_ALERTS_DOMAIN) {
            return null
        }

        val articleURLParam = url.toHttpUrlOrNull()?.queryParameter("url") ?: return null

        return try {
            URL(articleURLParam)
        } catch (e: MalformedURLException) {
            null
        }
    }

    private const val GOOGLE_ALERTS_DOMAIN = "www.google.com"
}
