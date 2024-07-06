package com.jocmp.capy.accounts

import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.net.URI

internal object LocalOkHttpClient {
    fun forAccount(path: URI): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(
                Cache(
                    directory = File(File(path), "http_cache"),
                    maxSize = 50L * 1024L * 1024L // 50 MiB
                )
            )
            .build()
    }
}
