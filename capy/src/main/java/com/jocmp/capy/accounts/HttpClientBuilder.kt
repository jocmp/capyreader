package com.jocmp.capy.accounts

import com.jocmp.capy.ClientCertManager
import com.jocmp.capy.UserAgentInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.net.URI
import java.util.concurrent.TimeUnit

private const val TIMEOUT_SECONDS = 30L

fun baseHttpClient() = baseHttpClientBuilder().build()

private fun baseHttpClientBuilder() =
    OkHttpClient
        .Builder()
        .addInterceptor(UserAgentInterceptor())
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)

fun OkHttpClient.Builder.clientCertAlias(manager: ClientCertManager, clientCertAlias: String): OkHttpClient.Builder {
    if (clientCertAlias.isNotEmpty()) {
        val clientSSlSocketFactory = manager.buildSslSocketFactory(clientCertAlias)
        sslSocketFactory(clientSSlSocketFactory.sslSocketFactory, clientSSlSocketFactory.trustManager)
    }
    return this
}

fun httpClientBuilder(cachePath: URI) =
    baseHttpClientBuilder()
        .cache(
            Cache(
                directory = File(File(cachePath), "http_cache"),
                maxSize = 50L * 1024L * 1024L // 50 MiB
            )
        )
