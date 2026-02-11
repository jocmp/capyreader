package com.jocmp.capy

import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response

class UserAgentInterceptor(private val userAgent: String = USER_AGENT) : Interceptor {
    override fun intercept(chain: Chain): Response {
        val originalRequest = chain.request()
        val requestWithUserAgent = originalRequest.newBuilder()
            .header("User-Agent", userAgent)
            .build()
        return chain.proceed(requestWithUserAgent)
    }

    companion object {
        const val USER_AGENT = "CapyReader (RSS Reader; https://capyreader.com/)"
    }
}

class BrowserHeadersInterceptor(
    private val userAgent: String,
    private val acceptLanguage: String,
) : Interceptor {
    override fun intercept(chain: Chain): Response {
        val originalRequest = chain.request()
        val request = originalRequest.newBuilder()
            .header("User-Agent", userAgent)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
            .header("Accept-Language", acceptLanguage)
            .header("Accept-Encoding", "gzip, deflate, br")
            .header("Sec-Fetch-Dest", "document")
            .header("Sec-Fetch-Mode", "navigate")
            .header("Sec-Fetch-Site", "none")
            .header("Sec-Fetch-User", "?1")
            .header("Upgrade-Insecure-Requests", "1")
            .build()
        return chain.proceed(request)
    }
}
