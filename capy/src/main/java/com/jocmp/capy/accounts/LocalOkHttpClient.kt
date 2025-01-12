package com.jocmp.capy.accounts

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.brotli.BrotliInterceptor
import java.net.URI

internal object LocalOkHttpClient {
    fun forAccount(path: URI): OkHttpClient {
        return httpClientBuilder(cachePath = path)
            .addInterceptor(BrotliInterceptor)
            .addNetworkInterceptor(CacheInterceptor())
            .build()
    }
}

// https://square.github.io/okhttp/features/interceptors/#rewriting-responses
class CacheInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        return response.newBuilder()
            .header("cache-control", "no-cache")
            .removeHeader("expires")
            .build()
    }
}
