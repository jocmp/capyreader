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
