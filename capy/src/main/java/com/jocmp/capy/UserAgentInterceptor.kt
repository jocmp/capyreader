package com.jocmp.capy

import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response
import okhttp3.internal.userAgent as OkHttpUserAgent
import java.io.IOException

class UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Chain): Response {
        val originalRequest = chain.request()
        val requestWithUserAgent = originalRequest.newBuilder()
            .header("User-Agent", USER_AGENT)
            .build()
        return chain.proceed(requestWithUserAgent)
    }

    companion object {
        const val USER_AGENT = "CapyReader (RSS Reader http://capyreader.com/) $OkHttpUserAgent"
    }
}
