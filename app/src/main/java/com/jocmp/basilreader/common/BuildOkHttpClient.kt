package com.jocmp.basilreader.common

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

fun buildOkHttpClient(context: Context): OkHttpClient {
    return OkHttpClient.Builder()
        .cache(
            Cache(
                directory = File(context.cacheDir, "http_cache"),
                maxSize = MAX_CACHE_SIZE
            )
        )
        .build()
}

const val MAX_CACHE_SIZE = 50L * 1024L * 1024L // 50 MiB
