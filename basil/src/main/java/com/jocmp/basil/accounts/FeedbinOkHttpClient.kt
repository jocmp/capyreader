package com.jocmp.basil.accounts

import com.jocmp.basil.AccountPreferences
import com.jocmp.feedbinclient.BasicAuthInterceptor
import okhttp3.Cache
import okhttp3.Credentials
import okhttp3.OkHttpClient
import java.io.File
import java.net.URI

object FeedbinOkHttpClient {
    fun forAccount(path: URI, preferences: AccountPreferences): OkHttpClient {
        val basicAuthInterceptor = BasicAuthInterceptor {
            val username = preferences.username.get()
            val password = preferences.password.get()

            Credentials.basic(username, password)
        }

        return OkHttpClient.Builder()
            .addInterceptor(basicAuthInterceptor)
            .cache(
                Cache(
                    directory = File(File(path), "http_cache"),
                    maxSize = 50L * 1024L * 1024L // 50 MiB
                )
            )
            .build()
    }
}
