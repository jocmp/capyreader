package com.jocmp.capy.accounts

import com.jocmp.capy.UserAgentInterceptor
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.brotli.BrotliInterceptor
import java.io.File
import java.net.URI

internal object LocalOkHttpClient {
    fun forAccount(path: URI): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(BrotliInterceptor)
            .addNetworkInterceptor(CacheInterceptor())
            .addInterceptor(UserAgentInterceptor())
            .cache(
                Cache(
                    directory = File(File(path), "http_cache"),
                    maxSize = 50L * 1024L * 1024L // 50 MiB
                )
            )
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
