package com.jocmp.capy.accounts

import okhttp3.Credentials
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
            .addInterceptor(LocalBasicAuthInterceptor())
            .build()
    }
}

class LocalBasicAuthInterceptor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val url = request.url

        if (url.username.isNotBlank() && url.password.isNotBlank()) {
            val basicAuth = Credentials.basic(url.username, url.password)

            val parsedURL = url.newBuilder()
                .username("")
                .password("")
                .build()

            val authenticatedRequest = request
                .newBuilder()
                .header("Authorization", basicAuth)
                .url(parsedURL)
                .build()

            return chain.proceed(authenticatedRequest)
        }

        return chain.proceed(request)
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
