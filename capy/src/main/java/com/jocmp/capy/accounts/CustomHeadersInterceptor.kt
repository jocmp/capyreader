package com.jocmp.capy.accounts

import okhttp3.Interceptor
import okhttp3.Response

internal class CustomHeadersInterceptor(
    private val headers: Map<String, String>,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        headers.forEach { (name, value) -> builder.header(name, value) }
        return chain.proceed(builder.build())
    }
}
